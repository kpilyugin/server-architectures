package server.impl.tcp;

import protocol.Protocol;
import server.Server;
import server.impl.MessageBuffer;
import util.InsertionSort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpNonBlockingServer extends Server {

  private final ExecutorService workerExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
  private ServerSocketChannel serverChannel;
  private Selector selector;

  @Override
  public void start() throws IOException {
    serverChannel = ServerSocketChannel.open();
    serverChannel.socket().setReuseAddress(true);
    serverChannel.socket().bind(new InetSocketAddress(Server.PORT));
    serverChannel.configureBlocking(false);
    selector = Selector.open();
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    startExecutor();
  }

  @Override
  protected void runServerLoop() {
    while (!isShutdown) {
      try {
        selector.select();
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          if (!key.isValid()) {
            continue;
          }
          if (key.isAcceptable()) {
            accept(key);
          } else if (key.isReadable()) {
            read(key);
          } else if (key.isWritable()) {
            write(key);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void accept(SelectionKey key) throws IOException {
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
    SocketChannel clientChannel = serverChannel.accept();

    clientChannel.configureBlocking(false);
    clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new MessageBuffer());
    statsHandler.onConnected(clientChannel.getRemoteAddress().hashCode());
  }

  private void read(SelectionKey key) throws IOException {
    if (isShutdown) {
      return;
    }
    MessageBuffer buffer = (MessageBuffer) key.attachment();
    final SocketChannel channel = (SocketChannel) key.channel();
    if (!buffer.canRead()) {
      return;
    }
    if (buffer.getBuffer().position() == 0) {
      statsHandler.onConnected(channel.getRemoteAddress().hashCode());
    }
    int[] array = buffer.tryReadMessage(channel);
    if (array != null) {
      statsHandler.onReceivedRequest(channel.getRemoteAddress().hashCode());
      workerExecutor.submit(() -> {
        try {
          handleClientRequest(array, channel, buffer);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }

  private void write(SelectionKey key) throws IOException {
    MessageBuffer buffer = (MessageBuffer) key.attachment();
    final SocketChannel channel = (SocketChannel) key.channel();
    if (!buffer.canWrite()) {
      return;
    }
    boolean finished = buffer.tryWriteResult(channel);
    if (finished) {
      statsHandler.onResponded(channel.getRemoteAddress().hashCode());
    }
  }

  private void handleClientRequest(int[] array, SocketChannel channel, MessageBuffer buffer) throws IOException {
    InsertionSort.sort(array);
    int id = channel.getRemoteAddress().hashCode();
    byte[] message = Protocol.toBytes(array);
    statsHandler.onSorted(id);
    buffer.setResult(message);
  }

  @Override
  public void shutdown() {
    super.shutdown();
    try {
      serverChannel.socket().close();
      serverChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    workerExecutor.shutdownNow();
  }

}
