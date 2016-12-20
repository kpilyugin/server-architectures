package server.impl.tcp;

import protocol.Protocol;
import server.impl.MessageBuffer;
import server.impl.ServerBase;
import util.InsertionSort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpAsyncServer extends ServerBase {

  private final ExecutorService workerExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
  private AsynchronousServerSocketChannel serverChannel;

  @Override
  public void start() throws IOException {
    serverChannel = AsynchronousServerSocketChannel.open();
    serverChannel.bind(new InetSocketAddress(PORT));
    serverChannel.accept(null, new AcceptHandler());
  }

  @Override
  protected void runServerLoop() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {
    }
  }

  @Override
  public void shutdown() {
    super.shutdown();
    try {
      serverChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    workerExecutor.shutdownNow();
  }

  private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
    @Override
    public void completed(AsynchronousSocketChannel channel, Void attachment) {
      try {
        statsHandler.connected(channel.getRemoteAddress().hashCode());
      } catch (IOException e) {
        e.printStackTrace();
      }
      MessageBuffer buffer = new MessageBuffer();
      channel.read(buffer.getBuffer(), channel, new ReadHandler(buffer));
      serverChannel.accept(null, this);
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
      exc.printStackTrace();
    }
  }

  private class ReadHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private final MessageBuffer messageBuffer;

    public ReadHandler(MessageBuffer messageBuffer) {
      this.messageBuffer = messageBuffer;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
      if (result == -1) {
        return;
      }

      int[] array = messageBuffer.getMessageIfReady();
      if (array != null) {
        try {
          statsHandler.receivedRequest(channel.getRemoteAddress().hashCode());
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
        workerExecutor.submit(() -> {
          try {
            InsertionSort.sort(array);
            statsHandler.sorted(channel.getRemoteAddress().hashCode());

            byte[] message = Protocol.toBytes(array);
            ByteBuffer resultBuffer = ByteBuffer.wrap(message);
            channel.write(resultBuffer, channel, new WriteHandler(resultBuffer));
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      } else {
        channel.read(messageBuffer.getBuffer(), channel, this);
      }
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel) {
      exc.printStackTrace();
    }
  }

  private class WriteHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private final ByteBuffer buffer;

    public WriteHandler(ByteBuffer buffer) {
      this.buffer = buffer;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
      if (buffer.hasRemaining()) {
        channel.write(buffer, channel, this);
      } else {
        try {
          statsHandler.responded(channel.getRemoteAddress().hashCode());
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
        MessageBuffer messageBuffer = new MessageBuffer();
        channel.read(messageBuffer.getBuffer(), channel, new ReadHandler(messageBuffer));
      }
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel) {
      exc.printStackTrace();
    }
  }
}
