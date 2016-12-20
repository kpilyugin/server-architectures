package client;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface Client {
  void connect(InetSocketAddress address) throws IOException;
  void sendMessage(int[] array) throws IOException;
  int[] receiveMessage() throws IOException;
  void shutdown() throws IOException;
}
