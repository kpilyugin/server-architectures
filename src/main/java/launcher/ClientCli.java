package launcher;

import client.ClientLauncher;
import client.ClientParameters;
import client.ClientType;

import java.io.IOException;

public class ClientCli {

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientParameters parameters = ClientParameters.builder()
        .type(ClientType.TCP_MULTI_CONNECTION)
        .numClients(1)
        .arraySize(100)
        .delay(100)
        .numRequests(5)
        .build();

    ClientLauncher.run(parameters);
  }
}
