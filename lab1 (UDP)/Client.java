import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

  private static String hostName;
  private static int portNumber;
  private static String requestString = "";

	public static void main(String[] args) throws IOException {

    if((args[2].equals("REGISTER") && args.length != 5) || (args[2].equals("LOOKUP") && args.length != 4) || (!args[2].equals("REGISTER") && !args[2].equals("LOOKUP"))){
        System.out.println("Usage: java Client <host_name> <port_number> <REGISTER || LOOKUP> <plate_number> <owner_name>");
        return;
    }
    else{
        // Checks valid plate number
        if(!validPlate(args[3])){
            System.out.println("Invalid plate number: ??-??-??");
            return;
        }

        // Parse host name and port number
        hostName = args[0];
        portNumber = Integer.parseInt(args[1]);

        // Parse request
        if(args[2].equals("REGISTER")){
            requestString = "REGISTER " + args[3] + " " + args[4];
        }
        else if(args[2].equals("LOOKUP")){
            requestString = "LOOKUP " + args[3];
        }
    }

    // Prepare connection
		DatagramSocket socket = new DatagramSocket();
		InetAddress ip = InetAddress.getByName(hostName);

    // Send request
    byte[] request = requestString.getBytes();
    DatagramPacket requestPacket = new DatagramPacket(request, request.length, ip, portNumber);
    socket.send(requestPacket);

    // Receive response
    byte[] response = new byte[1024];
    DatagramPacket responsePacket = new DatagramPacket(response, response.length);
    socket.receive(responsePacket);
    System.out.println("Server response: " + new String(responsePacket.getData()));
	}

  public static boolean validPlate(String plateNumber) {
    Pattern pattern = Pattern.compile("([A-Z0-9]{2}-){2}[A-Z0-9]{2}");
    Matcher matcher = pattern.matcher(plateNumber);
    return matcher.find();
  }
}
