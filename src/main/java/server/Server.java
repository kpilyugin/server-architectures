package server;

import java.io.IOException;

public abstract class Server {
  public static final int PORT = 3456;
  public static final int TIMEOUT = 1000;

  public abstract void start() throws IOException;
  public abstract void shutdown();

  public abstract void printStats();
}
