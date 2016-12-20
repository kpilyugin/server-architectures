package protocol;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ProtocolTest {
  @Test
  public void testProtocol() throws Exception {
    int[] array = new int[]{6, 10, -25, 4};

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Protocol.write(array, stream);

    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(stream.toByteArray()));
    int length = dataInputStream.readInt();
    byte[] bytes = new byte[length];
    dataInputStream.read(bytes);

    int[] result = Protocol.fromBytes(bytes);
    Assert.assertArrayEquals(array, result);
  }

  @Test
  public void testBytes() throws IOException {
    int[] array = new int[] {1, 1, 1, 2, 3, 4, 5, 6, 8, 9};
    byte[] bytes = Protocol.toBytes(array);

    byte[] message = new byte[bytes.length - 4];
    System.arraycopy(bytes, 4, message, 0, message.length);
    int[] newArray = Protocol.fromBytes(message);
    Assert.assertArrayEquals(array, newArray);
  }
}