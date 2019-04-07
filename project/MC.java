import java.net.InetAddress;
import java.net.UnknownHostException;

public class MC implements Runnable {

  private InetAddress address;

  public MC(String ip, int port){
    try{
      this.address = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  public void run(){

  }

}
