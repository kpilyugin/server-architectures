package launcher;

import benchmark.BenchmarkParams;
import benchmark.BenchmarkParams.VaryingType;
import benchmark.BenchmarkResult;
import benchmark.SingleResult;
import client.Client;
import client.ClientFactory;
import client.ClientType;
import org.apache.commons.io.FileUtils;
import server.Server;
import server.ServerType;
import util.ArrayUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.LongConsumer;

public class BenchmarkLauncher {

  private static final long PACKET_LOSS_PENALTY = 100;

  private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
  private final BenchmarkParams params;

  public BenchmarkLauncher(BenchmarkParams params) {
    this.params = params;
  }

  public static void main(String[] args) throws IOException {
    BenchmarkParams params = BenchmarkParams.builder()
        .numClients(5)
        .arraySize(1000)
        .delay(0)
        .numRequests(5)
        .hostName(args[0])
        .port(Server.PORT)
        .build();
    runAll(params, VaryingType.ARRAY_SIZE, 1000, 20000, 2000);
  }

  private static void runAll(BenchmarkParams params, VaryingType type, int from, int to, int step) throws IOException {
    params.setVaryingType(type);
    params.setVaryingFrom(from);
    params.setVaryingTo(to);
    params.setVaryingStep(step);

    File folder = new File("results", type.name());
    FileUtils.writeStringToFile(new File(folder, "params.txt"), params.getParamsInfo(), Charset.defaultCharset());

    for (ServerType serverType : ServerType.values()) {
      params.setType(serverType);
      BenchmarkResult benchmarkResult = new BenchmarkLauncher(params).run();
      try (FileWriter writer = new FileWriter(new File(folder, serverType.name() + ".csv"))) {
        writer.write("Request time on server, client time on server, client working time \n");
        for (SingleResult result : benchmarkResult.getResults()) {
          writer.write(result.getServerRequestTime() + ", " + result.getServerClientTime() + ", " + result.getClientWorkingTime() + "\n");
        }
      }
    }
  }

  public BenchmarkResult run() throws IOException {
    BenchmarkResult result = new BenchmarkResult(params);
    for (int value = params.getVaryingFrom(); value <= params.getVaryingTo(); value += params.getVaryingStep()) {
      switch (params.getVaryingType()) {
        case NUM_CLIENTS:
          params.setNumClients(value);
          break;
        case ARRAY_SIZE:
          params.setArraySize(value);
          break;
        case DELAY:
          params.setDelay(value);
          break;
      }
      SingleResult singleResult = runSingleCase();
      System.out.println("Single test case completed: " + singleResult);
      result.addResult(singleResult);
    }
    clientExecutor.shutdownNow();
    return result;
  }

  private SingleResult runSingleCase() throws IOException {
    System.out.println("Connecting to " + params.getHostName());
    try (Socket socket = new Socket(params.getHostName(), RemoteServerLauncher.PORT)) {
      System.out.println("connected!");
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
          int packetLosses = 0;
          Client client = ClientFactory.create(ClientType.forServerType(params.getType()));
          client.connect(new InetSocketAddress(params.getHostName(), params.getPort()));
          for (int j = 0; j < params.getNumRequests(); j++) {
            int[] array = new Random().ints(params.getArraySize(), 0, params.getArraySize()).toArray();
            int[] result = null;
            try {
              client.sendMessage(array);
              result = client.receiveMessage();
            } catch (SocketTimeoutException e) {
              System.out.println("timeout, sending again");
              j--;
              continue;
            }
            if (result == null) {
              packetLosses++;
            } else if (!ArrayUtil.isSorted(result)) {
              System.err.println("Array is not sorted");
            }
            Thread.sleep(params.getDelay());
          }
          client.shutdown();
          if (packetLosses != 0) {
            System.out.println("Lost " + packetLosses + " packets.");
          }
          return System.currentTimeMillis() - startTime;
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
          return Long.MAX_VALUE;
        }
      }));
    }
    return futures.stream().mapToLong(f -> {
      try {
        return f.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return Long.MAX_VALUE;
      }
    }).filter(l -> l != Long.MAX_VALUE).average().orElse(-1);
  }
}
