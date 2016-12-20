package server.impl;

import protocol.Protocol;
import util.InsertionSort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpNonBlockingServer extends TcpServerBase {

  private final ExecutorService workerExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
  private ServerSocketChannel serverChannel;
  private Selector selector;

  @Override
  public void start() throws IOException {
    serverChannel = ServerSocketChannel.open();
    serverChannel.bind(new InetSocketAddress(TcpServerBase.PORT));
    serverChannel.configureBlocking(false);
    selector = Selector.open();
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    startExecutor();
  }

  @Override
  protected void runServerLoop() {
    while (serverChannel.isOpen()) {
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
    clientChannel.register(selector, SelectionKey.OP_READ, new MessageBuffer());
    System.out.println("connected to " + clientChannel.getRemoteAddress());
  }

  private void read(SelectionKey key) throws IOException {
    MessageBuffer reader = (MessageBuffer) key.attachment();
    final SocketChannel channel = (SocketChannel) key.channel();
    if (Thread.currentThread().isInterrupted()) {
      return;
    }
    int[] array = reader.tryReadMessage(channel);
    if (array != null) {
      workerExecutor.submit(() -> {
        try {
          handleClientRequest(array, channel);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }

  private void handleClientRequest(int[] array, SocketChannel channel) throws IOException {
    InsertionSort.sort(array);

    byte[] message = Protocol.toBytes(array);
    ByteBuffer buffer = ByteBuffer.wrap(message);

    while (buffer.hasRemaining()) {
      channel.write(buffer);
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

}
