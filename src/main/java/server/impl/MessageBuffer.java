package server.impl;

import protocol.Protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static server.impl.MessageBuffer.State.*;

public class MessageBuffer {

  private final ByteBuffer buffer = ByteBuffer.allocate(Protocol.MAX_MESSAGE_SIZE);
  private volatile State state = READING;
  private ByteBuffer resultBuffer;

  enum State {
    READING, WAITING, WRITING;
  }

  public ByteBuffer getBuffer() {
    return buffer;
  }

  public boolean canRead() {
    return state == READING;
  }

  public boolean canWrite() {
    return state == WRITING;
  }

  public void setResult(byte[] result) {
    resultBuffer = ByteBuffer.wrap(result);
    state = WRITING;
  }

  public int[] tryReadMessage(SocketChannel channel) throws IOException {
    int read = channel.read(buffer);
    while (read > 0) {
      read = channel.read(buffer);
    }
    return getMessageIfReady();
  }

  public boolean tryWriteResult(SocketChannel channel) throws IOException {
    channel.write(resultBuffer);
    boolean finished = !resultBuffer.hasRemaining();
    if (finished) {
      state = READING;
    }
    return finished;
  }

  public int[] getMessageIfReady() {
    if (buffer.position() > Protocol.HEADER_SIZE) {
      int length = buffer.getInt(0) + Protocol.HEADER_SIZE;
      if (buffer.position() >= length) {
        buffer.flip();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        buffer.compact();
        state = State.WAITING;
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
