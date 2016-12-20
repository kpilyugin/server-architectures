package server.impl;

import protocol.Protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageBuffer {
  public static final int BUFFER_SIZE = 1000000;
  public static final int HEADER_SIZE = 4;

  private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

  public ByteBuffer getBuffer() {
    return buffer;
  }

  public int[] tryReadMessage(SocketChannel channel) throws IOException {
    int read = channel.read(buffer);
    while (read > 0) {
      read = channel.read(buffer);
    }
    return getMessageIfReady();
  }

  public int[] getMessageIfReady() {
    if (buffer.position() > HEADER_SIZE) {
      int length = buffer.getInt(0) + HEADER_SIZE;
      if (buffer.position() >= length) {
        buffer.flip();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        buffer.compact();
        try {
          return Protocol.fromBytes(bytes);
        } catch (IOException e) {
          return null;
        }
      }
    }
    return null;
  }
}
