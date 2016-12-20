package server.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpThreadPoolServer extends TcpSocketServer {
  private final ExecutorService workerExecutor = Executors.newCachedThreadPool();

  @Override
  protected void handleClient(Socket socket) throws IOException {
    workerExecutor.submit(() -> {
      while (!socket.isClosed()) {
        try {
          processClientRequest(socket);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public void shutdown() {
    super.shutdown();
    workerExecutor.shutdownNow();
  }
}
