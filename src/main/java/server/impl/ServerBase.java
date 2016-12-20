package server.impl;

import server.Server;
import stat.StatsHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ServerBase extends Server {
  protected final StatsHandler statsHandler = new StatsHandler();

  protected final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();

  public void startExecutor() {
    acceptExecutor.submit(this::runServerLoop);
  }

  protected abstract void runServerLoop();

  @Override
  public void shutdown() {
    acceptExecutor.shutdownNow();
  }

  @Override
  public void printStats() {
    double averageClientTime = statsHandler.getAverageClientTime();
    System.out.println("averageClientTime = " + averageClientTime);
    double averageRequestTime = statsHandler.getAverageRequestTime();
    System.out.println("averageRequestTime = " + averageRequestTime);
  }
}
