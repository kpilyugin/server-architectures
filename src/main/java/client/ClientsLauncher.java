package client;

import util.ArrayUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientsLauncher {
  public static void run(ClientParameters params) throws IOException, InterruptedException {
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < params.getNumClients(); i++) {
      threads.add(new Thread(() -> {
        try {
          long startTime = System.currentTimeMillis();
          Client client = ClientFactory.create(params.getType());
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
          long endTime = System.currentTimeMillis();
          System.out.println("client time = " + (endTime - startTime) / params.getNumRequests());
          client.shutdown();
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
        }
      }));
    }
    threads.forEach(Thread::start);
    for (Thread thread : threads) {
      thread.join();
    }
  }
}
