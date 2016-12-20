package server.impl.udp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpThreadPoolServer extends UdpServer {
  private final ExecutorService workerExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

  @Override
  protected void submitRequest(Runnable runnable) {
    workerExecutor.submit(runnable);
  }

  @Override
  public void shutdown() {
    super.shutdown();
    workerExecutor.shutdownNow();
  }
}
