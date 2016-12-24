package stat;

public class ClientStats {
  private static final double NANOS_TO_MILLIS = 1e-6;

  private volatile long timeConnected;
  private volatile long timeReceivedRequest;

  private volatile long clientTime = 0;
  private volatile long requestTime = 0;
  private volatile int numRequests = 0;

  public void onConnected() {
    timeConnected = System.nanoTime();
  }

  public void onReceivedRequest() {
    timeReceivedRequest = System.nanoTime();
    numRequests++;
  }

  public void onResponded() {
    if (timeReceivedRequest < timeConnected) {
      throw new IllegalStateException("Last snapshot of received request should be after connection");
    }
    clientTime += System.nanoTime() - timeConnected;
  }

  public void onSorted() {
    requestTime += System.nanoTime() - timeReceivedRequest;
  }

  public double getClientTime() {
    return NANOS_TO_MILLIS * clientTime / numRequests;
  }

  public double getRequestTime() {
    return NANOS_TO_MILLIS * requestTime / numRequests;
  }

  @Override
  public String toString() {
    return numRequests + " requests: " + requestTime + " - " + clientTime + "\n";
  }
}
