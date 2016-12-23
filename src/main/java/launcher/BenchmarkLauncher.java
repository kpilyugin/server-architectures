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
    ServerType serverType = ServerType.TCP_NON_BLOCKING;
    Server server = ServerFactory.create(serverType);
    server.start();

    ClientParameters parameters = ClientParameters.builder()
        .type(ClientType.forServerType(serverType))
        .numClients(20)
        .arraySize(10000)
        .delay(100)
        .numRequests(10)
        .hostName("localhost")
        .port(Server.PORT)
        .build();

    ClientsLauncher.run(parameters);
    server.printStats();
    server.shutdown();
    System.out.println("Benchmark finished");
  }
}
