package server.impl.udp;

public class UdpMultiThreadServer extends UdpServer {
  @Override
  protected void submitRequest(Runnable runnable) {
    new Thread(runnable).start();
  }
}
