import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Server {

  private static HashMap<String, String> database = new HashMap<String, String>();

  private static String serviceAddress;
	private static int servicePort;

	private static String multicastAddress;
	private static int multicastPort;

	public static void main(String[] args) throws IOException {

    if(args.length != 3){
        System.out.println("Usage: java Server <srvc_port> <mcast_addr> <mcast_port>");
        return;
    }
    else{
        // Parse arguments
        serviceAddress = getIPv4();
        servicePort = Integer.parseInt(args[0]);
        multicastAddress = args[1];
        multicastPort = Integer.parseInt(args[2]);
    }

    // open multicast socket
		MulticastSocket multicastSocket = new MulticastSocket();
		multicastSocket.setTimeToLive(1);
		InetAddress multicastAddress = InetAddress.getByName(multicastAddress);

    // open server socket
		DatagramSocket serverSocket = new DatagramSocket(servicePort);
		serverSocket.setSoTimeout(1000);

    // 1s interval advertisement control variables
		long elapsedTime = 1000;
		long prevTime = System.currentTimeMillis();

    while (true) {
      byte[] buf = new byte[256];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);

      try {
        // receive request
        serverSocket.receive(packet);
        String request = new String(packet.getData(), 0, packet.getLength());

        // Analyse client's request
        String[] requestArray = requestString.split(" ");
        byte[] response = new byte[1024];

        if(requestArray[0].equals("REGISTER")){
            response = register(requestArray[1], requestArray[2]).getBytes();
        }
        else if(requestArray[0].equals("LOOKUP")){
            response = lookup(requestArray[1]).getBytes();
        }

        // send response
        buf = response.getBytes();
        InetAddress cliAddress = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, cliAddress, port);
        serverSocket.send(packet);

        System.out.println(request + " :: " + response);
      } catch (SocketTimeoutException e) {
        System.out.println(e);
      }

      // BEGIN --- service advertisement every 1 second
      long currentTime = System.currentTimeMillis();

      elapsedTime += currentTime - prevTime;
      prevTime = currentTime;

      if (elapsedTime >= 1000) {
        elapsedTime -= 1000;

        String advertisement = serviceAddress + ":"  + Integer.toString(servicePort);
        packet = new DatagramPacket(advertisement.getBytes(),
            advertisement.getBytes().length, multicastAddress,
            multicastPort);
        multicastSocket.send(packet);

        System.out.println("multicast: " + multicastAddress + " " + multicastPort + ": " + serviceAddress + " " + servicePort);
      }
      // END ---service advertisement
    }

    // close server socket
    serverSocket.close();

    // close multicast socket
    multicastSocket.close();
	}

  public static String register(String plateNumber, String owner) {
    return database.put(plateNumber, owner) == null? Integer.toString(database.size()) : "-1";
  }

  public static String lookup(String plateNumber) {
    String owner = database.get(plateNumber);
    System.out.println(database.get(plateNumber));
    return owner == null ? "NOT_FOUND" : owner;
  }

  public static String getIPv4() {
		System.setProperty("java.net.preferIPv4Stack", "true");

		String ip = null;

		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();

				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		return ip;
	}
}
