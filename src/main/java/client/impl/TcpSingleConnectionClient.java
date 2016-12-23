package client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TcpSingleConnectionClient extends TcpClient {
  @Override
  public void connect(InetSocketAddress address) throws IOException {
    super.connect(address);
    openSocket();
  }

  @Override
  public void shutdown() throws IOException {
    closeSocket();
  }
}
