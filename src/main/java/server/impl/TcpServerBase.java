package server.impl;

import protocol.Protocol;
import server.Server;
import util.InsertionSort;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TcpServerBase extends Server {
  public static final int PORT = 3456;

  protected final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();

  protected static void log(String s) {
    System.out.println(s + ": " + System.currentTimeMillis());
  }

  public void startExecutor() {
    acceptExecutor.submit(this::runServerLoop);
  }

  protected abstract void runServerLoop();

  @Override
  public void shutdown() {
    acceptExecutor.shutdownNow();
  }

  protected void processClientRequest(Socket socket) throws IOException {
    int[] array = Protocol.read(socket.getInputStream());
    log("read array");
    InsertionSort.sort(array);
    log("sorted");
    Protocol.write(array, socket.getOutputStream());
    log("written array");
  }
}
