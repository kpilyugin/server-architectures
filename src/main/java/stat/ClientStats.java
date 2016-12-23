package stat;

public class ClientStats {
  private volatile long timeConnected;
  private volatile long timeReceivedRequest;

  private volatile long clientTime = 0;
  private volatile long requestTime = 0;
  private volatile int numRequests = 0;

  public void onConnected() {
    timeConnected = System.currentTimeMillis();
  }

  public void onReceivedRequest() {
    timeReceivedRequest = System.currentTimeMillis();
    numRequests++;
  }

  public void onResponded() {
    if (timeReceivedRequest < timeConnected) {
      throw new IllegalStateException("Last snapshot of received request should be after connection");
    }
    requestTime += System.currentTimeMillis() - timeConnected;
  }

  public void onSorted() {
    clientTime += System.currentTimeMillis() - timeReceivedRequest;
  }

  public long getClientTime() {
    return clientTime / numRequests;
  }

  public long getRequestTime() {
    return requestTime / numRequests;
  }

  @Override
  public String toString() {
    return numRequests + " requests: " + requestTime + " - " + clientTime + "\n";
  }
}
