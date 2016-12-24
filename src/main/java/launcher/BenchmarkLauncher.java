package launcher;

import client.Client;
import client.ClientFactory;
import client.ClientType;
import launcher.result.BenchmarkResult;
import launcher.result.SingleResult;
import server.Server;
import server.ServerType;
import util.ArrayUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class BenchmarkLauncher {

  private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
  private final BenchmarkParams params;

  public BenchmarkLauncher(BenchmarkParams params) {
    this.params = params;
  }

  public static void main(String[] args) throws IOException {
    ServerType serverType = ServerType.UDP_THREAD_POOL;
    BenchmarkParams parameters = BenchmarkParams.builder()
        .type(serverType)
        .numClients(5)
        .arraySize(1000)
        .delay(100)
        .numRequests(10)
        .hostName("localhost")
        .port(Server.PORT)
        .varyingType(BenchmarkParams.Varying.NUM_CLIENTS)
        .varyingFrom(1)
        .varyingTo(10)
        .step(2)
        .build();

    BenchmarkResult result = new BenchmarkLauncher(parameters).run();
    System.out.println("Benchmark finished");
    System.out.println(result);
  }

  public BenchmarkResult run() throws IOException {
    BenchmarkResult result = new BenchmarkResult(params);
    for (int value = params.getVaryingFrom(); value <= params.getVaryingTo(); value += params.getStep()) {
      switch (params.getVaryingType()) {
        case NUM_CLIENTS:
          params.setNumClients(value);
          break;
        case NUM_REQUESTS:
          params.setNumRequests(value);
          break;
        case DELAY:
          params.setDelay(value);
          break;
      }
      SingleResult singleResult = runSingleCase();
      System.out.println("Single test case completed: " + singleResult);
      result.addResult(singleResult);
    }
    return result;
  }

  private SingleResult runSingleCase() throws IOException {
    try (Socket socket = new Socket(params.getHostName(), RemoteServerLauncher.PORT)) {
      DataInputStream input = new DataInputStream(socket.getInputStream());
      DataOutputStream output = new DataOutputStream(socket.getOutputStream());

      output.write(params.getType().ordinal());
      int started = input.read();
      if (started != 1) {
        throw new IllegalStateException("Remote server not started");
      }
      double clientWorkingTime = runClients();
      output.write(1);
      double serverRequestTime = input.readDouble();
      double serverClientTime = input.readDouble();
      return new SingleResult(serverRequestTime, serverClientTime, clientWorkingTime);
    }
  }

  private double runClients() {
    List<Future<Long>> futures = new ArrayList<>();
    for (int i = 0; i < params.getNumClients(); i++) {
      futures.add(clientExecutor.submit(() -> {
        try {
          long startTime = System.currentTimeMillis();
          Client client = ClientFactory.create(ClientType.forServerType(params.getType()));
          client.connect(new InetSocketAddress(params.getHostName(), params.getPort()));
          for (int j = 0; j < params.getNumRequests(); j++) {
            int[] array = new Random().ints(params.getArraySize(), 0, params.getArraySize()).toArray();
            client.sendMessage(array);
            int[] result = client.receiveMessage();
            if (!ArrayUtil.isSorted(result)) {
              System.err.println("Array is not sorted");
            }
            Thread.sleep(params.getDelay());
          }
          client.shutdown();
          return System.currentTimeMillis() - startTime;
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
          return Long.MAX_VALUE;
        }
      }));
    }
    return futures.stream().collect(Collectors.averagingLong(f -> {
      try {
        return f.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return Long.MAX_VALUE;
      }
    }));
  }
}
