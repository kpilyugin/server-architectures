package server;

import stat.ServerStats;
import stat.StatsHandler;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Server {
  public static final int PORT = 10456;
  public static final int TIMEOUT = 10000;

  private final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();
  protected final StatsHandler statsHandler = new StatsHandler();
  protected volatile boolean isShutdown;

  public abstract void start() throws IOException;

  protected void startExecutor() {
    acceptExecutor.submit(this::runServerLoop);
  }

  protected abstract void runServerLoop();

  public void shutdown() {
    isShutdown = true;
    acceptExecutor.shutdownNow();
  }

  public ServerStats collectStats() {
    return statsHandler.collectStats();
  }
}
