package stat;

import org.junit.Assert;
import org.junit.Test;

public class ClientStatsTest {

  public static final int N = 10;

  @Test
  public void testComputeStats() throws InterruptedException {
    ClientStats stats = new ClientStats();
    for (int i = 0; i < N; i++) {
      stats.onConnected();
      Thread.sleep(50);
      stats.onReceivedRequest();
      Thread.sleep(100);
      stats.onSorted();
      Thread.sleep(50);
      stats.onResponded();
    }
    System.out.println(stats);
    Assert.assertEquals(200, stats.getRequestTime(), 40);
    Assert.assertEquals(100, stats.getClientTime(), 20);
  }
}