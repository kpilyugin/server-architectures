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
    startExecutor();
  }

  @Override
  protected void runServerLoop() {
    while (!serverSocket.isClosed()) {
      try {
        Socket socket = serverSocket.accept();
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
      statsHandler.onConnected(id);
      int[] array = Protocol.read(socket.getInputStream());
      statsHandler.onReceivedRequest(id);

      InsertionSort.sort(array);
      statsHandler.onSorted(id);

      Protocol.write(array, socket.getOutputStream());
      statsHandler.onResponded(id);
    } catch (EOFException ignored) {
    }
  }
}
