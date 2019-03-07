import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

  private static String multicastAddress;
  private static int multicastPort;
  private static String requestString = "";

	public static void main(String[] args) throws IOException {

    if((args[2].equals("REGISTER") && args.length != 5) || (args[2].equals("LOOKUP") && args.length != 4) || (!args[2].equals("REGISTER") && !args[2].equals("LOOKUP"))){
        System.out.println("Usage: java Client <mcast_addr> <mcast_port> <REGISTER || LOOKUP> <plate_number> <owner_name>");
        return;
    }
    else{
        // Checks valid plate number
        if(!validPlate(args[3])){
            System.out.println("Invalid plate number: ??-??-??");
            return;
        }

        // Parse host name and port number
        multicastAddress = args[0];
        multicastPort = Integer.parseInt(args[1]);

        // Parse request
        if(args[2].equals("REGISTER")){
            requestString = "REGISTER " + args[3] + " " + args[4];
        }
        else if(args[2].equals("LOOKUP")){
            requestString = "LOOKUP " + args[3];
        }
    }

    InetAddress group = InetAddress.getByName(multicastAddress);
    MulticastSocket multicastSocket = new MulticastSocket(multicastPort);
    multicastSocket.joinGroup(group);

    byte[] buf = new byte[256];
		DatagramPacket multicastPacket = new DatagramPacket(buf, buf.length);
		multicastSocket.receive(multicastPacket);

    String msg = new String(multicastPacket.getData());
		String[] parts = msg.split(":");
		serviceAddress = parts[0];
		servicePort = Integer.parseInt(parts[1].replaceAll("[^\\d.]", ""));

    System.out.println("multicast: " + multicastAddress + " " + multicastPort + ": " + serviceAddress + " " + servicePort);

    // open socket
		DatagramSocket socket = new DatagramSocket();

    // send request
		buf = requestString.getBytes();
		InetAddress address = InetAddress.getByName(serviceAddress);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, servicePort);
		socket.send(packet);

    // receive response
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		String response = new String(packet.getData(), 0, packet.getLength());
    
		System.out.println(requestString + " :: " + response);

    // close socket
		socket.close();
		multicastSocket.leaveGroup(group);
		multicastSocket.close();
	}

  public static boolean validPlate(String plateNumber) {
    Pattern pattern = Pattern.compile("([A-Z0-9]{2}-){2}[A-Z0-9]{2}");
    Matcher matcher = pattern.matcher(plateNumber);
    return matcher.find();
  }
}
