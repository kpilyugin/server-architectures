package server.impl.tcp;

import java.io.IOException;
import java.net.Socket;

public class TcpSingleThreadServer extends TcpSocketServer {
  @Override
  protected void handleClient(Socket socket) throws IOException {
    processClientRequest(socket);
  }
}
