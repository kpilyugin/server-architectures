package launcher;

import client.ClientType;
import lombok.Builder;
import lombok.Data;
import server.ServerType;

@Builder
@Data
public class BenchmarkParams {
  private ServerType type;
  private int arraySize;
  private int numClients;
  private long delay;
  private int numRequests;
  private String hostName;
  private int port;
  private Varying varyingType;
  private int varyingFrom;
  private int varyingTo;
  private int step;

  public enum Varying {
    NUM_REQUESTS, NUM_CLIENTS, DELAY
  }
}
