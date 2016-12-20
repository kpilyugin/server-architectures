package server.impl;

import server.Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ServerBase extends Server {
  protected final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();

  protected static void log(String s) {
    System.out.println(s);
  }

  public void startExecutor() {
    acceptExecutor.submit(this::runServerLoop);
  }

  protected abstract void runServerLoop();

  @Override
  public void shutdown() {
    acceptExecutor.shutdownNow();
  }
}
