package launcher.result;

import launcher.BenchmarkParams;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BenchmarkResult {
  private final BenchmarkParams params;
  private final List<SingleResult> results = new ArrayList<>();

  public BenchmarkResult(BenchmarkParams params) {
    this.params = params;
  }

  public List<SingleResult> getResults() {
    return results;
  }

  public void addResult(SingleResult singleResult) {
    results.add(singleResult);
  }
}
