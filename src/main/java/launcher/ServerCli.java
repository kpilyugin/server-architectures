package launcher;

import server.Server;
import server.ServerFactory;
import server.ServerType;

import java.io.IOException;
import java.util.Scanner;

public class ServerCli {
  public static void main(String[] args) {
    ServerType type = ServerType.TCP_SINGLE_THREAD;
    Server server = ServerFactory.create(type);
    try {
      server.start();

      System.out.println("Type 'exit' to exit");
      try (Scanner scanner = new Scanner(System.in)) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          switch (line) {
            case "exit":
              server.shutdown();
              System.exit(0);
            case "stats":
              server.printStats();
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
