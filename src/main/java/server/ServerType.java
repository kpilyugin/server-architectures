package server;

public enum ServerType {
  TCP_SINGLE_THREAD("TCP, single thread"),
  TCP_MULTI_THREAD("TCP, new thread for connection"),
  TCP_THREAD_POOL("TCP, worket thread pool"),
  TCP_NON_BLOCKING("TCP, non-blocking"),
  TCP_ASYNC("TCP, async"),
  UDP_MULTI_THREAD("UDP, new thread for request"),
  UDP_THREAD_POOL("UDP, thread pool");

  private final String desc;

  ServerType(String desc) {
    this.desc = desc;
  }

  @Override
  public String toString() {
    return desc;
  }
}
