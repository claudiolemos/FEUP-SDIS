class App {

  private static int replicationDegree, reclaimSpace;
  private static String filepath, peer;

  public static void main(String[] args) {
    if(!validArgs(args))
      return;

    Data data = new Data("./teste.txt",2);
    System.out.println(data.chunks.size());
    for(int i = 0; i < data.chunks.size(); i++)
      System.out.println(data.chunks.get(i).body);
  }

  private static boolean validArgs(String[] args) {
    if(args.length < 2) return false;

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
