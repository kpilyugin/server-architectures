package server;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import protocol.Protocol;
import server.impl.ServerBase;
import util.ArrayUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class TcpServerTest {
  public static final int X = 10;

  private Server server;

  @After
  public void shutdown() {
    server.shutdown();
  }

  @Test
  public void testSingleThread() throws IOException {
    server = ServerFactory.create(ServerType.TCP_SINGLE_THREAD);
    server.start();
    for (int i = 0; i < X; i++) {
      Socket socket = new Socket("localhost", ServerBase.PORT);
      sendAndCheck(socket);
      socket.close();
    }
  }

  @Test
  public void testMultiThread() throws IOException {
    testPermanent(ServerType.TCP_MULTI_THREAD);
  }

  @Test
  public void testThreadPool() throws IOException {
    testPermanent(ServerType.TCP_THREAD_POOL);
  }

  @Test
  public void testAsync() throws IOException {
    testPermanent(ServerType.TCP_ASYNC);
  }

  @Test
  public void testNonBlocking() throws IOException {
    testPermanent(ServerType.TCP_NON_BLOCKING);
  }

  private void testPermanent(ServerType type) throws IOException {
    server = ServerFactory.create(type);
    server.start();
    Socket socket = new Socket("localhost", ServerBase.PORT);

    for (int i = 0; i < X; i++) {
      sendAndCheck(socket);
    }
    socket.close();
    server.shutdown();
  }

  private static void sendAndCheck(Socket socket) throws IOException {
    int[] array = new Random().ints(100, 0, 10).toArray();

    Protocol.write(array, socket.getOutputStream());
    int[] result = Protocol.read(socket.getInputStream());
    Assert.assertTrue(ArrayUtil.isSorted(result));
  }
}