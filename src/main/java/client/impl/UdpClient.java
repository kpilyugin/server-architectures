package client.impl;

import client.Client;
import protocol.Protocol;
import server.Server;

import java.io.IOException;
import java.net.*;

public class UdpClient implements Client {
  private InetSocketAddress serverAddress;
  private DatagramSocket socket;
  private byte[] packetBytes = new byte[Protocol.MAX_MESSAGE_SIZE];

  @Override
  public void connect(InetSocketAddress address) throws IOException {
    serverAddress = address;
    socket = new DatagramSocket();
    socket.setSoTimeout(Server.TIMEOUT);
  }

  @Override
  public void sendMessage(int[] array) throws IOException {
    byte[] message = Protocol.toBytes(array);
    DatagramPacket packet = new DatagramPacket(message, message.length,
        serverAddress.getAddress(), serverAddress.getPort());
    socket.send(packet);
  }

  @Override
  public int[] receiveMessage() throws IOException {
    DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
    try {
      socket.receive(packet);
    } catch (SocketTimeoutException ignored) {
      return null;
    }
    return Protocol.fromBytes(packetBytes);
  }

  @Override
  public void shutdown() throws IOException {
    socket.close();
  }
}
