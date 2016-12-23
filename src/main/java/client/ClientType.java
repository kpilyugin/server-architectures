package client;

import server.ServerType;

public enum ClientType {
  TCP_SINGLE_CONNECTION,
  TCP_MULTI_CONNECTION,
  UDP;

  public static ClientType forServerType(ServerType serverType) {
    switch (serverType) {
      case TCP_SINGLE_THREAD:
        return TCP_MULTI_CONNECTION;
      case TCP_MULTI_THREAD:
      case TCP_THREAD_POOL:
      case TCP_NON_BLOCKING:
      case TCP_ASYNC:
        return TCP_SINGLE_CONNECTION;
      case UDP_MULTI_THREAD:
      case UDP_THREAD_POOL:
      default:
        return UDP;
    }
  }
}
