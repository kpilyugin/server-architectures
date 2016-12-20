package protocol;

import java.io.*;

import static protocol.Message.ArrayMessage;

public class Protocol {
  public static int[] read(InputStream stream) throws IOException {
    int messageLength = new DataInputStream(stream).readInt();
    byte[] bytes = new byte[messageLength];
    int readCount = 0;
    while (readCount < bytes.length) {
      readCount += stream.read(bytes, readCount, bytes.length - readCount);
    }
    return fromBytes(bytes);
  }

  public static void write(int[] array, OutputStream stream) throws IOException {
    ArrayMessage message = createMessage(array);
    new DataOutputStream(stream).writeInt(message.getSerializedSize());
    message.writeTo(stream);
  }

  public static int[] fromBytes(byte[] bytes) throws IOException {
    ArrayMessage message = ArrayMessage.parseFrom(bytes);
    int[] array = new int[message.getValueCount()];
    for (int i = 0; i < array.length; i++) {
      array[i] = message.getValue(i);
    }
    return array;
  }

  public static byte[] toBytes(int[] array) throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    write(array, stream);
    return stream.toByteArray();
  }

  private static ArrayMessage createMessage(int[] array) {
    ArrayMessage.Builder builder = ArrayMessage.newBuilder();
    for (int value : array) {
      builder.addValue(value);
    }
    return builder.build();
  }
}
