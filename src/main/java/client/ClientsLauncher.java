package client;

import util.ArrayUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientsLauncher {
  public static void run(ClientParameters params) throws IOException, InterruptedException {
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < params.numClients; i++) {
      threads.add(new Thread(() -> {
        try {
          long startTime = System.currentTimeMillis();
          Client client = ClientFactory.create(params.type);
          for (int j = 0; j < params.numRequests; j++) {
            int[] array = new Random().ints(params.arraySize, 0, params.arraySize).toArray();
            client.sendMessage(array);
            int[] result = client.receiveMessage();
            if (!ArrayUtil.isSorted(result)) {
              System.err.println("Array is not sorted");
            }
            Thread.sleep(params.delay);
          }
          long endTime = System.currentTimeMillis();
          System.out.println("client time = " + (endTime - startTime) / params.numRequests);
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
