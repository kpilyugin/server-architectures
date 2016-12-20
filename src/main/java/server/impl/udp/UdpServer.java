package server.impl.udp;

import protocol.Protocol;
import server.impl.MessageBuffer;
import server.impl.ServerBase;
import util.InsertionSort;

import java.io.IOException;
import java.net.*;

public abstract class UdpServer extends ServerBase {
  protected DatagramSocket datagramSocket;
  protected DatagramPacket packet;

  @Override
  public void start() throws IOException {
    datagramSocket = new DatagramSocket(PORT);
    datagramSocket.setSoTimeout(TIMEOUT);
    packet = new DatagramPacket(new byte[MessageBuffer.BUFFER_SIZE], MessageBuffer.BUFFER_SIZE);
    startExecutor();
  }

  @Override
  protected void runServerLoop() {
    while (!datagramSocket.isClosed()) {
      try {
        datagramSocket.receive(packet);
        int[] array = Protocol.fromBytes(packet.getData());
        submitRequest(() -> processClientRequest(array, packet.getSocketAddress()));
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

    try {
      byte[] result = Protocol.toBytes(array);
      DatagramPacket response = new DatagramPacket(result, result.length, address);
      datagramSocket.send(response);
    } catch (SocketException ignored) {
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
