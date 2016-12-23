package client;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientParameters {
  private ClientType type;
  private int arraySize;
  private int numClients;
  private long delay;
  private int numRequests;
  private String hostName;
  private int port;
}
