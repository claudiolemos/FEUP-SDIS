import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
    System.out.println("BACKUP" + filepath + replicationDegree);
  }


  public void restore(String filepath){System.out.println("RESTORE" + filepath);}
  public void delete(String filepath){System.out.println("DELETE" + filepath);}
  public void reclaim(int reclaimSpace){System.out.println("RECLAIM" + reclaimSpace);}
  public void state(){System.out.println("STATE");}
}
