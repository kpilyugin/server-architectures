package launcher;

import protocol.Protocol;
import server.impl.ServerBase;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

public class ClientCli {

  public void run() throws IOException {
    Socket socket = new Socket("localhost", ServerBase.PORT);
    for (int i = 0; i < 1000; i++) {

      int[] array = new Random().ints(10, 0, 10).toArray();
      System.out.println("array: " + Arrays.toString(array));

      Protocol.write(array, socket.getOutputStream());
      int[] result = Protocol.read(socket.getInputStream());
      System.out.println("result = " + Arrays.toString(result));
    }
    socket.close();
  }

  public static void main(String[] args) throws IOException {
    new ClientCli().run();
  }
}
