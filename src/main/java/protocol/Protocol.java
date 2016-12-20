package protocol;

import java.io.*;

import static protocol.Message.*;

public class Protocol {
  public static int[] read(InputStream stream) throws IOException {
    int length = new DataInputStream(stream).readInt();
    ArrayMessage message = ArrayMessage.parseDelimitedFrom(stream);
    int[] array = new int[length];
    if (length != message.getValueCount()) {
      throw new IllegalArgumentException("Wrong array length");
    }
    for (int i = 0; i < length; i++) {
      array[i] = message.getValue(i);
    }
    return array;
  }

  public static void write(int[] array, OutputStream stream) throws IOException {
    new DataOutputStream(stream).writeInt(array.length);
    ArrayMessage.Builder builder = ArrayMessage.newBuilder();
    for (int value : array) {
      builder.addValue(value);
    }
    ArrayMessage message = builder.build();
    message.writeDelimitedTo(stream);
  }

  public int[] readBytes(byte[] bytes) throws IOException {
    return read(new ByteArrayInputStream(bytes));
  }

  public byte[] getBytes(ArrayMessage message) throws IOException {
    return message.toByteArray();
  }
}
