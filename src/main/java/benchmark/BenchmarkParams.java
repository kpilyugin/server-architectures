package benchmark;

import lombok.Builder;
import lombok.Data;
import server.ServerType;

import static benchmark.BenchmarkParams.VaryingType.ARRAY_SIZE;
import static benchmark.BenchmarkParams.VaryingType.DELAY;
import static benchmark.BenchmarkParams.VaryingType.NUM_CLIENTS;

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
    ARRAY_SIZE("Array size"),
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

  public String getParamsInfo() {
    return "Array size: " +
        (varyingType == ARRAY_SIZE ? varyingDesc() : arraySize) +
        "\nClients count: " +
        (varyingType == NUM_CLIENTS ? varyingDesc() : numClients) +
        "\nRequests count: " +
        numRequests +
        "\nDelay: " +
        (varyingType == DELAY ? varyingDesc() : delay);
  }

  private String varyingDesc() {
    return "from " + varyingFrom + " to " + varyingTo + " by " + varyingStep;
  }
}
