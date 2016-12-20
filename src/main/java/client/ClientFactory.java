package client;

import client.impl.TcpMultiConnectionClient;
import client.impl.TcpSingleConnectionClient;
import client.impl.UdpClient;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ClientFactory {
  private static final Map<ClientType, Supplier<Client>> FACTORIES = new EnumMap<>(ClientType.class);

  static {
    FACTORIES.put(ClientType.TCP_SINGLE_CONNECTION, TcpSingleConnectionClient::new);
    FACTORIES.put(ClientType.TCP_MULTI_CONNECTION, TcpMultiConnectionClient::new);
    FACTORIES.put(ClientType.UDP, UdpClient::new);
  }

  public static Client create(ClientType type) {
    return FACTORIES.get(type).get();
  }
}
