package client;

import client.impl.TcpPermanentClient;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ClientFactory {
  private static final Map<ClientType, Supplier<Client>> FACTORIES = new EnumMap<>(ClientType.class);

  static {
    FACTORIES.put(ClientType.TCP_PERMANENT, TcpPermanentClient::new);
    FACTORIES.put(ClientType.TCP_TEMPORARY, TcpPermanentClient::new);
    FACTORIES.put(ClientType.UDP, TcpPermanentClient::new);
  }

  public static Client create(ClientType type) {
    return FACTORIES.get(type).get();
  }
}
