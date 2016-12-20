package server.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public abstract class TcpSocketServer extends TcpServerBase {

  protected ServerSocket serverSocket;

  @Override
  public void start() throws IOException {
    serverSocket = new ServerSocket(PORT);
    serverSocket.setSoTimeout(1000);
    System.out.println("Server started at port " + PORT);
    startExecutor();
  }

  @Override
  protected void runServerLoop() {
    while (!serverSocket.isClosed()) {
      try {
        Socket socket = serverSocket.accept();
        log("new connection: " + socket.getInetAddress());
        handleClient(socket);
      } catch (SocketTimeoutException | SocketException ignored) {

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected abstract void handleClient(Socket socket) throws IOException;

  @Override
  public void shutdown() {
    super.shutdown();
    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
