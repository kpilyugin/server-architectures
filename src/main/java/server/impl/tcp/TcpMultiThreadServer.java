package server.impl.tcp;

import java.io.IOException;
import java.net.Socket;

public class TcpMultiThreadServer extends TcpSocketServer {
  @Override
  protected void handleClient(Socket socket) {
    new Thread(() -> {
      while (!socket.isClosed() && !isShutdown) {
        try {
          processClientRequest(socket);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
