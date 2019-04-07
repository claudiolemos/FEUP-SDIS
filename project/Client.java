import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class Client {

  private static int replicationDegree, reclaimSpace;
  private static String filepath, host, peer;

  public static void main(String[] args) {
    if(!validArgs(args))
      return;

    try{
      Registry registry = LocateRegistry.getRegistry(host);
      RMI rmi = (RMI) registry.lookup(peer);

      switch (args[1]) {
        case "BACKUP":
          rmi.backup(filepath, replicationDegree);
          break;
        case "RESTORE":
          rmi.restore(filepath);
          break;
        case "DELETE":
          rmi.delete(filepath);
          break;
        case "RECLAIM":
          rmi.reclaim(reclaimSpace);
          break;
        case "STATE":
          rmi.state();
          break;
      }
    } catch (Exception e) {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
  }

  private static boolean validArgs(String[] args) {
    if(args.length < 2) return false;

    String[] ip = args[0].split("/");
    host = ip[0];
    peer = ip[1];

    switch (args[1]) {
      case "BACKUP":
        if(args.length != 4) return false;
        filepath = args[2];
        replicationDegree = Integer.parseInt(args[3]);
        break;
      case "RESTORE":
        if(args.length != 3) return false;
        filepath = args[2];
        break;
      case "DELETE":
        if(args.length != 3) return false;
        filepath = args[2];
        break;
      case "RECLAIM":
        if(args.length != 3) return false;
        reclaimSpace = Integer.parseInt(args[2]);
        break;
      case "STATE":
        if(args.length != 2) return false;
        break;
      default:
        return false;
    }
    return true;
  }
}
