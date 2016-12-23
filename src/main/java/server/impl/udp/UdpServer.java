package server.impl.udp;

import protocol.Protocol;
import server.Server;
import util.InsertionSort;

import java.io.IOException;
import java.net.*;

public abstract class UdpServer extends Server {
  protected DatagramSocket datagramSocket;
  protected DatagramPacket packet;

  @Override
  public void start() throws IOException {
    datagramSocket = new DatagramSocket(PORT);
    datagramSocket.setSoTimeout(TIMEOUT);
    byte[] bytes = new byte[Protocol.MAX_MESSAGE_SIZE];
    packet = new DatagramPacket(bytes, bytes.length);
    startExecutor();
  }

  @Override
  protected void runServerLoop() {
    while (!isShutdown) {
      try {
        datagramSocket.receive(packet);
        int[] array = Protocol.fromBytes(packet.getData());
        SocketAddress address = packet.getSocketAddress();
        int id = address.hashCode();
        statsHandler.onConnected(id);
        statsHandler.onReceivedRequest(id);
        submitRequest(() -> processClientRequest(array, address));
      } catch (SocketTimeoutException | SocketException ignored) {
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void shutdown() {
    super.shutdown();
    datagramSocket.close();
  }

  protected abstract void submitRequest(Runnable runnable);

  private void processClientRequest(int[] array, SocketAddress address) {
    InsertionSort.sort(array);
    int id = address.hashCode();
    statsHandler.onSorted(id);

    try {
      byte[] result = Protocol.toBytes(array);
      DatagramPacket response = new DatagramPacket(result, result.length, address);
      datagramSocket.send(response);
      statsHandler.onResponded(id);
    } catch (SocketException ignored) {
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
