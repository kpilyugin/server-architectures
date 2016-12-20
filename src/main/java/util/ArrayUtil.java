package util;

import java.util.Random;

public class ArrayUtil {
  public static int[] randomArray(int size) {
    return new Random().ints(size, 0, size).toArray();
  }

  public static boolean isSorted(int[] array) {
    for (int i = 0; i < array.length - 1; i++) {
      if (array[i] > array[i + 1]) {
        return false;
      }
    }
    return true;
  }
}
