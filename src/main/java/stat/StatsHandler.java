package stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StatsHandler {
  private final Map<Integer, StatsHolder> clientStats = new ConcurrentHashMap<>();

  public void connected(int id) {
    StatsHolder stats = clientStats.getOrDefault(id, new StatsHolder());
    stats.timeConnected = System.currentTimeMillis();
    clientStats.put(id, stats);
  }

  public void receivedRequest(int id) {
    StatsHolder stats = clientStats.get(id);
    stats.timeReceived = System.currentTimeMillis();
    stats.numRequests++;
  }

  public void sorted(int id) {
    clientStats.get(id).sorted();
  }

  public void responded(int id) {
    clientStats.get(id).responded();
  }

  public double getAverageClientTime() {
    return clientStats.values().stream()
        .collect(Collectors.averagingDouble(stats -> stats.clientTime / stats.numRequests));
  }

  public double getAverageRequestTime() {
    return clientStats.values().stream()
        .collect(Collectors.averagingDouble(stats -> stats.requestsTime / stats.numRequests));
  }
}
