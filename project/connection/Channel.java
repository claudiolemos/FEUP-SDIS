package connection;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.io.IOException;
import java.net.UnknownHostException;

import threads.ReceiveMessage;

public class Channel implements Runnable {

  private InetAddress address;
  private int port;

  public Channel(String ip, int port){
    this.port = port;
    try{
      this.address = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  public void send(byte[] message){
    try(DatagramSocket socket = new DatagramSocket()){
      DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
      socket.send(packet);
    } catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  public void run(){
    byte[] buffer = new byte[64512];
    try{
      MulticastSocket socket = new MulticastSocket(port);
      socket.joinGroup(address);
      while(true){
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        connection.Peer.execute(new ReceiveMessage(Arrays.copyOf(buffer, packet.getLength())));
      }
    }catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

}
