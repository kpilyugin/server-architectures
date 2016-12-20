package server.impl;

import java.io.IOException;
import java.net.Socket;

public class TcpMultiThreadServer extends TcpSocketServer {
  @Override
  protected void handleClient(Socket socket) {
    new Thread(() -> {
      while (!socket.isClosed()) {
        try {
          processClientRequest(socket);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
