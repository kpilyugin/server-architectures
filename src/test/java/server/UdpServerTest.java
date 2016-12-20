package server;

import client.Client;
import client.ClientFactory;
import client.ClientType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.TestUtil;

import java.io.IOException;
import java.util.Random;

public class UdpServerTest {
  private static final int X = 100;

  private Server server;
  private Client client;

  @Before
  public void setUp() {
    client = ClientFactory.create(ClientType.UDP);
  }

  @After
  public void shutdown() throws IOException {
    client.shutdown();
    server.shutdown();
  }

  @Test
  public void testMultiThread() throws IOException {
    test(ServerType.UDP_MULTI_THREAD);
  }

  @Test
  public void testThreadPool() throws IOException {
    test(ServerType.UDP_THREAD_POOL);
  }

  private void test(ServerType type) throws IOException {
    server = ServerFactory.create(type);
    client.connect(null);
    server.start();

    int numLost = 0;
    for (int i = 0; i < X; i++) {
      int[] array = new Random().ints(100, 0, 10).toArray();
      client.sendMessage(array);
      int[] result = client.receiveMessage();
      if (result == null) {
        numLost++;
      } else {
        TestUtil.assertSorted(result);
      }
    }
    System.out.println("Number of lost packets: " + numLost);
  }
}