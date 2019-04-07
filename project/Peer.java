import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Peer implements RMI{

  private static double version;
  private static int id, mcPort, mdbPort, mdrPort;
  private static String accessPoint, mcIP, mdbIP, mdrIP;
  private static MC mc;
  private static ScheduledThreadPoolExecutor threadPool;

  public static void main(String[] args) {
    if(!validArgs(args))
      return;

    mc = new MC(mcIP, mcPort);

    try{
      Peer peer = new Peer();
      RMI rmi = (RMI) UnicastRemoteObject.exportObject(peer, 0);
      Registry registry = LocateRegistry.getRegistry();
      registry.bind(accessPoint, rmi);
      System.out.println("Peer " + id + " ready");
    } catch (Exception e) {
			System.err.println("Peer exception: " + e.toString());
			e.printStackTrace();
    }

    // threadPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);
    // threadPool.execute(mc);
  }

  private static boolean validArgs(String[] args) {
    if(args.length != 9) return false;
    version = Double.parseDouble(args[0]);
    id = Integer.parseInt(args[1]);
    accessPoint = args[2];
    mcIP = args[3];
    mcPort = Integer.parseInt(args[4]);
    mdbIP = args[5];
    mdbPort = Integer.parseInt(args[6]);
    mdrIP = args[7];
    mdrPort = Integer.parseInt(args[8]);
    return true;
  }

  public void backup(String filepath, int replicationDegree){
    Data file = new Data(filepath, replicationDegree);

    for(int i = 0; i < file.getChunks().size(); i++){
      try{
        Chunk chunk = file.getChunks().get(i);
        String header = "PUTCHUNK " + version + " " + id + " " + file.getID() + " " + chunk.getNumber() + " " + replicationDegree + "\r\n\r\n";
        System.out.println("Sent " + header.substring(0,header.length() - 4));
        byte[] message = Utils.concatenate(header.getBytes("US-ASCII"), chunk.getBody());
      }
      catch (UnsupportedEncodingException e) {
        System.err.println(e.toString());
        e.printStackTrace();
      }
    }
  }


  public void restore(String filepath){System.out.println("RESTORE" + filepath);}
  public void delete(String filepath){System.out.println("DELETE" + filepath);}
  public void reclaim(int reclaimSpace){System.out.println("RECLAIM" + reclaimSpace);}
  public void state(){System.out.println("STATE");}
}
