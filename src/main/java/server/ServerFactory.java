package server;

import server.impl.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import static server.ServerType.*;

public class ServerFactory {
  private static final Map<ServerType, Supplier<Server>> FACTORIES = new EnumMap<>(ServerType.class);

  static {
    FACTORIES.put(TCP_SINGLE_THREAD, TcpSingleThreadServer::new);
    FACTORIES.put(TCP_MULTI_THREAD, TcpMultiThreadServer::new);
    FACTORIES.put(TCP_THREAD_POOL, TcpThreadPoolServer::new);
    FACTORIES.put(TCP_NON_BLOCKING, TcpNonBlockingServer::new);
    FACTORIES.put(TCP_ASYNC, TcpAsyncServer::new);
  }

  public static Server create(ServerType type) {
    return FACTORIES.get(type).get();
  }
}
