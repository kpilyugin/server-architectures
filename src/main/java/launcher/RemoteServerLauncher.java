package launcher;

import server.Server;
import server.ServerFactory;
import server.ServerType;
import stat.ServerStats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteServerLauncher {
  public static final int PORT = 10432;

  public static void main(String[] args) throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("Started server at port " + PORT);
      while (!serverSocket.isClosed()) {
        Socket socket = serverSocket.accept();
        System.out.println("Connected to " + socket.getRemoteSocketAddress());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        Server server = null;
        try {
          int typeIndex = input.read();
          ServerType serverType = ServerType.values()[typeIndex];
          server = ServerFactory.create(serverType);
          server.start();
          System.out.println("Started server, type = " + serverType);
          output.write(1);

          int finish = input.read();
          if (finish != 1) {
            throw new IllegalStateException("Incorrect finish");
          }
          ServerStats stats = server.collectStats();
          output.writeDouble(stats.getAverageRequestTime());
          output.writeDouble(stats.getAverageClientTime());
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          if (server != null) {
            server.shutdown();
          }
        }
      }
    }
  }
}
