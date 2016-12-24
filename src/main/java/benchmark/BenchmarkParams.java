package benchmark;

import lombok.Builder;
import lombok.Data;
import server.ServerType;

@Builder
@Data
public class BenchmarkParams {
  private String hostName;
  private int port;
  private ServerType type;
  private int arraySize;
  private int numClients;
  private long delay;
  private int numRequests;
  private VaryingType varyingType;
  private int varyingFrom;
  private int varyingTo;
  private int varyingStep;

  public enum VaryingType {
    NUM_REQUESTS("Requests count"),
    NUM_CLIENTS("Clients count"),
    DELAY("Delay");

    private final String desc;

    VaryingType(String desc) {
      this.desc = desc;
    }

    @Override
    public String toString() {
      return desc;
    }
  }
}
