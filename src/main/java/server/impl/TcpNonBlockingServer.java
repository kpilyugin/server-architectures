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
  private ServerSocketChannel serverChannel;
  private Selector selector;
  private ExecutorService workerExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

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
    clientChannel.register(selector, SelectionKey.OP_READ, new MessagesBuffer());
    System.out.println("connected to " + clientChannel.getRemoteAddress());
  }

  private void read(SelectionKey key) throws IOException {
    MessagesBuffer reader = (MessagesBuffer) key.attachment();
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

  private static class MessagesBuffer {
    private static final int BUFFER_SIZE = 1000000;

    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    private int[] tryReadMessage(SocketChannel channel) throws IOException {
      int read = channel.read(buffer);
      while (read > 0) {
        read = channel.read(buffer);
      }
      System.out.println("tryRead: position = " + buffer.position());
      if (buffer.position() > 4) {
        int length = buffer.getInt(0);
        System.out.println("Length should be " + length);
        if (buffer.position() >= 4 + length) {
          buffer.flip();
          buffer.getInt();
          byte[] bytes = new byte[length];
          buffer.get(bytes);
          buffer.compact();
          return Protocol.fromBytes(bytes);
        }
      }
      return null;
    }
  }
}
