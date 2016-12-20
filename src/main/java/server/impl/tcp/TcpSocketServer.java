package server.impl.tcp;

import protocol.Protocol;
import server.Server;
import util.InsertionSort;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public abstract class TcpSocketServer extends Server {

  protected ServerSocket serverSocket;

  @Override
  public void start() throws IOException {
    serverSocket = new ServerSocket(PORT);
    serverSocket.setSoTimeout(TIMEOUT);
    System.out.println("Server started at port " + PORT);
    startExecutor();
  }

  @Override
  protected void runServerLoop() {
    while (!serverSocket.isClosed()) {
      try {
        Socket socket = serverSocket.accept();
        int id = socket.getRemoteSocketAddress().hashCode();
        statsHandler.connected(id);
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

  protected void processClientRequest(Socket socket) throws IOException {
    try {
      int id = socket.getRemoteSocketAddress().hashCode();
      int[] array = Protocol.read(socket.getInputStream());
      statsHandler.receivedRequest(id);

      InsertionSort.sort(array);
      statsHandler.sorted(id);

      Protocol.write(array, socket.getOutputStream());
      statsHandler.responded(id);
    } catch (EOFException ignored) {
    }
  }
}
