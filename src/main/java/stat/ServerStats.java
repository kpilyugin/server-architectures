package stat;

import lombok.Data;

@Data
public class ServerStats {
  private final double averageRequestTime;
  private final double averageClientTime;
}
