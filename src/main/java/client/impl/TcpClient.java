package client.impl;

import client.Client;
import protocol.Protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class TcpClient implements Client {
  private InetSocketAddress socketAddress;
  private Socket socket;

  @Override
  public void connect(InetSocketAddress address) throws IOException {
    socketAddress = address;
  }

  protected void openSocket() throws IOException {
    socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());
    socket.setSoTimeout(10000);
  }

  protected void closeSocket() throws IOException {
    socket.close();
    socket = null;
  }

  @Override
  public void sendMessage(int[] array) throws IOException {
    Protocol.write(array, socket.getOutputStream());
  }

  @Override
  public int[] receiveMessage() throws IOException {
    return Protocol.read(socket.getInputStream());
  }
}
