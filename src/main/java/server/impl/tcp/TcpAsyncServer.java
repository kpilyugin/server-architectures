package server.impl.tcp;

import protocol.Protocol;
import server.Server;
import server.impl.MessageBuffer;
import util.InsertionSort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpAsyncServer extends Server {

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
      Thread.sleep(TIMEOUT);
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
        MessageBuffer buffer = new MessageBuffer();
        int id = channel.getRemoteAddress().hashCode();
        channel.read(buffer.getBuffer(), channel, new ReadHandler(buffer, id));
        serverChannel.accept(null, this);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
      if (!isShutdown) {
        System.err.println("async accept failed:");
        exc.printStackTrace();
      }
    }
  }

  private class ReadHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private final MessageBuffer messageBuffer;
    private final int clientId;

    public ReadHandler(MessageBuffer messageBuffer, int id) {
      this.messageBuffer = messageBuffer;
      statsHandler.onConnected(id);
      clientId = id;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
      if (result == -1) {
        return;
      }

      int[] array = messageBuffer.getMessageIfReady();
      if (array != null) {
        statsHandler.onReceivedRequest(clientId);
        workerExecutor.submit(() -> {
          try {
            InsertionSort.sort(array);
            statsHandler.onSorted(clientId);
            byte[] message = Protocol.toBytes(array);
            ByteBuffer resultBuffer = ByteBuffer.wrap(message);
            channel.write(resultBuffer, channel, new WriteHandler(resultBuffer, clientId));
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
    private final int clientId;

    public WriteHandler(ByteBuffer buffer, int id) {
      this.buffer = buffer;
      clientId = id;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
      if (buffer.hasRemaining()) {
        channel.write(buffer, channel, this);
      } else {
        try {
          statsHandler.onResponded(channel.getRemoteAddress().hashCode());
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
        MessageBuffer messageBuffer = new MessageBuffer();
        channel.read(messageBuffer.getBuffer(), channel, new ReadHandler(messageBuffer, clientId));
      }
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel) {
      exc.printStackTrace();
    }
  }
}
