package launcher;

import server.Server;
import server.ServerFactory;
import server.ServerType;

import java.io.IOException;
import java.util.Scanner;

public class ServerCli {
  public static void main(String[] args) {
    ServerType type = ServerType.TCP_MULTI_THREAD;
    Server server = ServerFactory.create(type);
    try {
      server.start();

      System.out.println("Type 'exit' to exit");
      try (Scanner scanner = new Scanner(System.in)) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          if (line.equals("exit")) {
            server.shutdown();
            System.exit(0);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      server.shutdown();
    }
  }
}
