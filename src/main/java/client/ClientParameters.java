package client;

import lombok.Builder;

@Builder
public class ClientParameters {
  public ClientType type;
  public int arraySize;
  public int numClients;
  public long delay;
  public int numRequests;
}
