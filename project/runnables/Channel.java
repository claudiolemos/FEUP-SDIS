package connection;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.io.IOException;
import java.net.UnknownHostException;

import runnables.Receive;

/**
 * class used to represent a multicast channel thread
 */
public class Channel implements Runnable {

  /**
   * ip address of peer's host
   */
  private InetAddress address;
  /**
   * port through which the channel will be connected
   */
  private int port;

  /**
   * Channel constructor
   * @param ip   ip used to connect to the multicast
   * @param port port used to connect to the multicast
   */
  public Channel(String ip, int port){
    this.port = port;
    try{
      this.address = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  /**
   * sends a message to the multicast channel
   * @param message content of the message being sent
   */
  public void send(byte[] message){
    try(DatagramSocket socket = new DatagramSocket()){
      DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
      socket.send(packet);
    } catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  /**
   * loop used for the channel thread, initiating a Receive thread when a new message is received
   */
  public void run(){
    byte[] buffer = new byte[64512];
    try{
      MulticastSocket socket = new MulticastSocket(port);
      socket.joinGroup(address);
      while(true){
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        connection.Peer.execute(new Receive(Arrays.copyOf(buffer, packet.getLength())));
      }
    }catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }
}
