package stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StatsHandler {
  private final Map<Integer, ClientStats> clientStats = new ConcurrentHashMap<>();

  public void onConnected(int id) {
    clientStats.putIfAbsent(id, new ClientStats());
    clientStats.get(id).onConnected();
  }

  public void onReceivedRequest(int id) {
    clientStats.get(id).onReceivedRequest();
  }

  public void onSorted(int id) {
    clientStats.get(id).onSorted();
  }

  public void onResponded(int id) {
    clientStats.get(id).onResponded();
  }

  public double getAverageClientTime() {
    return clientStats.values().stream()
        .collect(Collectors.averagingDouble(ClientStats::getClientTime));
  }

  public double getAverageRequestTime() {
    return clientStats.values().stream()
        .collect(Collectors.averagingDouble(ClientStats::getRequestTime));
  }

  public ServerStats collectStats() {
    return new ServerStats(getAverageRequestTime(), getAverageClientTime());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    clientStats.values().forEach(builder::append);
    builder.append("\n]\n");
    return builder.toString();
  }
}
