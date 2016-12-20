package client.impl;

import client.Client;
import protocol.Protocol;
import server.Server;

import java.io.IOException;
import java.net.Socket;

public abstract class TcpClient implements Client {
  protected Socket socket;

  protected void openSocket() throws IOException {
    socket = new Socket("localhost", Server.PORT);
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
