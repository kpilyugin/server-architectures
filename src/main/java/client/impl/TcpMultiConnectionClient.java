package client.impl;

import java.io.IOException;

public class TcpMultiConnectionClient extends TcpClient {
  @Override
  public void sendMessage(int[] array) throws IOException {
    openSocket();
    super.sendMessage(array);
  }

  @Override
  public int[] receiveMessage() throws IOException {
    int[] array = super.receiveMessage();
    closeSocket();
    return array;
  }

  @Override
  public void shutdown() {

  }
}
