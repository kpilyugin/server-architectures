package server;

import stat.StatsHandler;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Server {
  public static final int PORT = 3456;
  public static final int TIMEOUT = 1000;

  protected final StatsHandler statsHandler = new StatsHandler();
  protected final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();
  protected volatile boolean isShutdown;

  public abstract void start() throws IOException;

  public void startExecutor() {
    acceptExecutor.submit(this::runServerLoop);
  }

  protected abstract void runServerLoop();

  public void shutdown() {
    isShutdown = true;
    acceptExecutor.shutdownNow();
  }

  public void printStats() {
    System.out.println(statsHandler);
    double averageClientTime = statsHandler.getAverageClientTime();
    System.out.println("averageClientTime = " + averageClientTime);
    double averageRequestTime = statsHandler.getAverageRequestTime();
    System.out.println("averageRequestTime = " + averageRequestTime);
  }
}
