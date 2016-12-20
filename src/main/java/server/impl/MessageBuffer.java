package server.impl;

import protocol.Protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageBuffer {

  private final ByteBuffer buffer = ByteBuffer.allocate(Protocol.MAX_MESSAGE_SIZE);

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
    if (buffer.position() > Protocol.HEADER_SIZE) {
      int length = buffer.getInt(0) + Protocol.HEADER_SIZE;
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
