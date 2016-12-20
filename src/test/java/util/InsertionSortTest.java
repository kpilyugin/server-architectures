package util;

import org.junit.Test;

public class InsertionSortTest {
  @Test
  public void testSort() {
    testSort(new int[]{1, 2});
    testSort(new int[]{2, 1});
    testSort(new int[]{-5, 2, 5, -15, 0, 10});
    testSort(new int[]{1});
  }
  
  private void testSort(int[] a) {
    InsertionSort.sort(a);
    TestUtil.assertSorted(a);
  }
}