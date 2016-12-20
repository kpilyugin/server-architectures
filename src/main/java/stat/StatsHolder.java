package stat;

public class StatsHolder {
  public volatile long timeConnected;
  public volatile long timeReceived;
  public volatile long timeSorted;
  public volatile long timeResponded;

  public volatile long clientTime = 0;
  public volatile long requestsTime = 0;
  public volatile int numRequests = 0;

  public void responded() {
    timeResponded = System.currentTimeMillis();
    requestsTime += timeResponded - timeReceived;
  }

  public void sorted() {
    timeSorted = System.currentTimeMillis();
    clientTime += timeSorted - timeReceived;
  }
}
