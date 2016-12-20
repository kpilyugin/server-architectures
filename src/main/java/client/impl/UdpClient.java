package client.impl;

import client.Client;
import protocol.Protocol;
import server.Server;

import java.io.IOException;
import java.net.*;

public class UdpClient implements Client {
  private InetAddress serverAddress;
  private DatagramSocket socket;
  private byte[] packetBytes = new byte[Protocol.MAX_MESSAGE_SIZE];

  @Override
  public void connect(InetSocketAddress address) throws IOException {
    try {
      serverAddress = InetAddress.getByName("localhost");
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    socket = new DatagramSocket();
    socket.setSoTimeout(Server.TIMEOUT);
  }

  @Override
  public void sendMessage(int[] array) throws IOException {
    byte[] message = Protocol.toBytes(array);
    DatagramPacket packet = new DatagramPacket(message, message.length, serverAddress, Server.PORT);
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
