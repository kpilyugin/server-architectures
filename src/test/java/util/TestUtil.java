package util;

import static org.junit.Assert.fail;

public class TestUtil {
  public static void assertSorted(int[] array) {
    for (int i = 0; i < array.length - 1; i++) {
      if (array[i] > array[i + 1]) {
        fail("Not sorted");
      }
    }
  }
}
