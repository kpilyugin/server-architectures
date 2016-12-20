package launcher;

import client.ClientsLauncher;
import client.ClientParameters;
import client.ClientType;
import server.Server;
import server.ServerFactory;
import server.ServerType;

import java.io.IOException;

public class BenchmarkLauncher {

  public static void main(String[] args) throws IOException, InterruptedException {
    Server server = ServerFactory.create(ServerType.TCP_NON_BLOCKING);
    server.start();

    ClientParameters parameters = ClientParameters.builder()
        .type(ClientType.TCP_MULTI_CONNECTION)
        .numClients(10)
        .arraySize(1000)
        .delay(100)
        .numRequests(5)
        .build();

    ClientsLauncher.run(parameters);
    server.printStats();
    server.shutdown();
  }
}
