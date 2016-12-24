package benchmark;

import lombok.Data;

@Data
public class SingleResult {
  private final double serverRequestTime;
  private final double serverClientTime;
  private final double clientWorkingTime;
}
