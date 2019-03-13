import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Server {

  private static HashMap<String, String> database = new HashMap<String, String>();
  private static int portNumber;

	public static void main(String[] args) throws IOException {

    if(args.length != 1){
        System.out.println("Usage: java Server <port_number>");
        return;
    }
    else{
        // Parse port number
        portNumber = Integer.parseInt(args[0]);
    }

    // Start connection
    DatagramSocket socket = new DatagramSocket(portNumber);

    // Server cycle
    while(true){
      // Receive client's request
      byte[] request  = new byte[1024];
      DatagramPacket requestPacket = new DatagramPacket(request, request.length);
      socket.receive(requestPacket);
      String requestString = new String(requestPacket.getData());
      System.out.println("Client request: " + requestString);

      // Analyse client's request
      String[] requestArray = requestString.split(" ");
      byte[] response = new byte[1024];

      if(requestArray[0].equals("REGISTER")){
          response = register(requestArray[1], requestArray[2]).getBytes();
      }
      else if(requestArray[0].equals("LOOKUP")){
          response = lookup(requestArray[1]).getBytes();
      }

      // Estabilish connection to client
      int port = requestPacket.getPort();
      InetAddress address = requestPacket.getAddress();

      // Send response to client
      DatagramPacket responsePacket = new DatagramPacket(response, response.length, address, port);
      socket.send(responsePacket);
    }
	}

  public static String register(String plateNumber, String owner) {
    return database.put(plateNumber, owner) == null? Integer.toString(database.size()) : "-1";
  }

  public static String lookup(String plateNumber) {
    String owner = database.get(plateNumber);
    System.out.println(database.get(plateNumber));
    return owner == null ? "NOT_FOUND" : owner;
  }
}
