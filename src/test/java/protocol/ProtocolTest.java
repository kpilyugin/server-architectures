package protocol;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ProtocolTest {
  @Test
  public void testBytes() throws IOException {
    int[] array = new int[] {1, 1, 1, 2, 3, 4, 5, 6, 8, 9};
    byte[] bytes = Protocol.toBytes(array);
    int[] result = Protocol.fromBytes(bytes);
    Assert.assertArrayEquals(array, result);
  }
}