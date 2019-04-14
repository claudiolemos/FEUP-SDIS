package connection;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.Arrays;
import java.io.File;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import database.*;
import runnables.*;
import utils.Utils;

/**
 * class that represents the peer that receives messages from the client
 */
public class Peer implements RMI{

  /**
   * version of the protocols being used
   */
  private static double version;
  /**
   * peer's id
   */
  private static int id;
  /**
   * port that the peer is using to connect to the multicast control channel
   */
  private static int mcPort;
  /**
   * port that the peer is using to connect to the multicast data channel
   */
  private static int mdbPort;
  /**
   * port that the peer is using to connect to the multicast recovery channel
   */
  private static int mdrPort;
  /**
   * name being used for the peer's remote object
   */
  private static String accessPoint;
  /**
   * ip that the peer is using to connect to the multicast control channel
   */
  private static String mcIP;
  /**
   * ip that the peer is using to connect to the multicast data channel
   */
  private static String mdbIP;
  /**
   * ip that the peer is using to connect to the multicast recovery channel
   */
  private static String mdrIP;
  /**
   * pool used to schedule the threads being executed by a peer
   */
  private static ScheduledThreadPoolExecutor threads;
  /**
   * control channel
   */
  private static Channel mc;
  /**
   * data channel
   */
  private static Channel mdb;
  /**
   * data recovery channel
   */
  private static Channel mdr;
  /**
   * peer's database
   */
  private static Database database;

  /**
   * Main peer function
   * @param args the arguments received from the terminal
   */
  public static void main(String[] args) {
    if(!validArgs(args))
      return;

    threads = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(500);
    mc = new Channel(mcIP, mcPort);
    mdb = new Channel(mdbIP, mdbPort);
    mdr = new Channel(mdrIP, mdrPort);

    try{
      Peer peer = new Peer();
      RMI rmi = (RMI) UnicastRemoteObject.exportObject(peer, 0);
      Registry registry = LocateRegistry.getRegistry();
      registry.rebind(accessPoint, rmi);
      System.out.println("Peer " + id + " ready\n");
    } catch (Exception e) {
			System.err.println("Peer exception: " + e.toString());
			e.printStackTrace();
    }

    loadDatabase();
    execute(mc);
    execute(mdb);
    execute(mdr);

    Runtime.getRuntime().addShutdownHook(new Thread(Peer::saveDatabase));
  }

  /**
   * checks if valids passed to the peer are valid
   * @param  args the arguments received from the terminal
   * @return boolean on wether or not the args are valid
   */
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

  /**
   * loads the file and backsup its chunks by sending PUTCHUNK messages via the multicast data channel
   * @param filepath          path to the file being backedup
   * @param replicationDegree desired replication degree for the file
   */
  public synchronized void backup(String filepath, int replicationDegree){
    Data file = new Data(filepath, replicationDegree);
    database.addFile(filepath, file);

    if(file.exists())
      System.out.println("Loaded " + filepath + " (" + file.getChunks().size() + " chunks). Initiating backup.\n");
    else {
      System.out.println(filepath + " doesn't exist.\n");
      return;
    }

    for(int i = 0; i < file.getChunks().size(); i++){
      try{
        Chunk chunk = file.getChunks().get(i);
        String header = "PUTCHUNK";
        if(version == 2.0) header += "ENH";
        header += " " + version + " " + id + " " + file.getID() + " " + chunk.getNumber() + " " + replicationDegree + "\r\n\r\n";
        System.out.println("Sending " + header.substring(0,header.length() - 4) + "\n");
        byte[] message = Utils.concatenate(header.getBytes(), chunk.getBody());
        database.addReplicationDegree(chunk.getID(),0);

        int counter = 1, timer = 1000;

        do {
          System.out.println("PUTCHUNK Try #" + counter + "\n");
          execute(new Send(message, Utils.Channel.MDB));
          Thread.sleep(timer);
          timer *= 2;
          counter++;
        } while (database.getReplicationDegree(chunk.getID()) < replicationDegree && counter < 6);
      }
      catch (InterruptedException e) {
        System.err.println(e.toString());
        e.printStackTrace();
      }
    }
  }

  /**
   * sends DELETE messages to the multicast control channel for the other peers to delete their chunks of the specified file
   * @param filepath path to the file being deleted
   */
  public synchronized void delete(String filepath){
    if(database.hasFile(filepath)){
      System.out.println("Initiating deletion of " + filepath + ".\n");
      String header = "DELETE " + version + " " + id + " " + database.getFile(filepath).getID() + "\r\n\r\n";
      database.deleteFile(filepath);
      System.out.println("Sending " + header.substring(0,header.length() - 4) + "\n");
      execute(new Send(header.getBytes(), Utils.Channel.MC));
    }
    else
      System.out.println(filepath + " hasn't been backedup by this peer yet, so it can't be deleted.\n");
  }

  /**
   * sends GETCHUNK messages to the multicast control channel in order to retrieve the chunks of the specified file from the other peers
   * @param filepath path to the file being restored
   */
  public synchronized void restore(String filepath){
    if(database.hasFile(filepath)){
      System.out.println("Initiating restoration of " + filepath + ".\n");
      try{
        for(int i = 0; i < database.getFile(filepath).getChunks().size(); i++){
            database.addWantedChunk(Utils.getChunkID(database.getFile(filepath).getID(),database.getFile(filepath).getChunks().get(i).getNumber()),false);
            String header = "GETCHUNK " + version + " " + id + " " + database.getFile(filepath).getID() + " " + database.getFile(filepath).getChunks().get(i).getNumber() + "\r\n\r\n";
            System.out.println("Sending " + header.substring(0,header.length() - 4) + "\n");

            int counter = 1, timer = 1000;

            do {
              System.out.println("GETCHUNK Try #" + counter + "\n");
              execute(new Send(header.getBytes(), Utils.Channel.MC));
              Thread.sleep(timer);
              timer *= 2;
              counter++;
            } while (!database.hasWantedChunk(Utils.getChunkID(database.getFile(filepath).getID(),database.getFile(filepath).getChunks().get(i).getNumber())) && counter < 6);
        }

        File file = new File("storage/peer" + getID() + "/restored/" + Paths.get(filepath).getFileName().toString());
        if(!file.exists()){
          file.getParentFile().mkdirs();
          file.createNewFile();
        }
        FileOutputStream fileStream = new FileOutputStream("storage/peer" + getID() + "/restored/" + Paths.get(filepath).getFileName().toString());

        for(int i = 0; i < database.getFile(filepath).getChunks().size(); i++)
            fileStream.write(database.getRestoredChunks().get(Utils.getChunkID(database.getFile(filepath).getID(),database.getFile(filepath).getChunks().get(i).getNumber())).getBody());

        System.out.println(filepath + " was restored.\n");
      } catch (InterruptedException | IOException e) {
        System.err.println(e.toString());
        e.printStackTrace();
      }
    }
    else
      System.out.println(filepath + " hasn't been backedup by this peer yet, so it can't be restored.\n");
  }

  /**
   * chooses what chunks are going to be deleted in order to achieved the desired reclaim space, giving priority to chunks that have exceeded their desired replication degree
   * @param reclaimSpace desired reclaim space
   */
  public synchronized void reclaim(int reclaimSpace){
    System.out.println("Initiating reclaim on peer " + id + ".\n");
    database.setAvailableSpace(reclaimSpace*1000 - database.getUsedSpace());

    if (database.getAvailableSpace() < 0){
      ArrayList<Chunk> priorityChunks = new ArrayList<>();
      ArrayList<Chunk> otherChunks = new ArrayList<>();

      for(Iterator<Map.Entry<String, Chunk>> iterator = database.getBackupChunks().entrySet().iterator(); iterator.hasNext();){
        Map.Entry<String, Chunk> entry = iterator.next();
        String chunkID = entry.getKey();
        Chunk chunk = entry.getValue();
        if(database.getReplicationDegree(chunkID) > chunk.getReplicationDegree())
          priorityChunks.add(chunk);
        else
          otherChunks.add(chunk);
      }

      reclaimAux(priorityChunks);
      reclaimAux(otherChunks);
    }

    System.out.println("Peer " + id + " can now store up to " + database.getSpace()/1000 + "kb\n");
  }

  /**
   * sends the REMOVED messages necessary to the multicast control channel until the peer reaches the desired reclaim space
   * @param chunks ArrayList of chunks
   */
  private synchronized void reclaimAux(ArrayList<Chunk> chunks){
    for(int i = 0; i < chunks.size(); i++){
      if(database.getAvailableSpace() < 0){
        database.removeBackupChunk(chunks.get(i).getID(), chunks.get(i));
        String header = "REMOVED " + version + " " + id + " " + chunks.get(i).getFileID() + " " + chunks.get(i).getNumber() + "\r\n\r\n";
        System.out.println("Sending " + header.substring(0,header.length() - 4) + "\n");
        execute(new Send(header.getBytes(), Utils.Channel.MC));
      }
      else
        break;
    }
  }

  /**
   * prints out the state of the peer
   */
  public synchronized void state(){
    System.out.println("Peer " + id + " state:");
    printFilesInfo();
    printBackupChunksInfo();
    printStorageInfo();
  }

  /**
   * prints out info about the peer's files
   */
  public void printFilesInfo(){
    System.out.println("  Files");
    for (Map.Entry<String, Data> entry : database.getFiles().entrySet()) {
      Data file = entry.getValue();
      System.out.println("    File");
      System.out.println("      Filepath: " + file.getPath());
      System.out.println("      ID: " + file.getID());
      System.out.println("      Desired Replication Degree: " + file.getReplicationDegree());
      System.out.println("      Chunks");
      for(int i = 0; i < file.getChunks().size(); i++){
        System.out.println("        Chunk");
        System.out.println("          ID:" + file.getChunks().get(i).getID());
        System.out.println("          Perceived Replication Degree:" + database.getReplicationDegree(file.getChunks().get(i).getID()));
      }
    }
  }

  /**
   * prints out info about the peer's backed up chunks
   */
  public void printBackupChunksInfo(){
    System.out.println("  Stored chunks");
    for (Map.Entry<String, Chunk> entry : database.getBackupChunks().entrySet()) {
      Chunk chunk = entry.getValue();
      System.out.println("    Chunk");
      System.out.println("      ID: " + chunk.getID());
      System.out.println("      Size: " + chunk.getSize()/1000.0 + "kb");
      System.out.println("      Perceived Replication Degree:" + database.getReplicationDegree(chunk.getID()));
    }
  }

  /**
   * prinst out info about the peer's storage
   */
  public void printStorageInfo(){
    System.out.println("  Storage");
    System.out.println("    Used: " + database.getUsedSpace()/1000.0 + "kb");
    System.out.println("    Disk Space: "+ (database.getAvailableSpace() + database.getUsedSpace())/1000 + "kb\n");
  }

  /**
   * adds a thread to the thread pool and executes it
   * @param thread runnable thread to be executed
   */
  public static void execute(Runnable thread){
    threads.execute(thread);
  }

  /**
   * adds a thread to the thread pool and schedules it to be executed in a certain time
   * @param thread runnable thread to be executed
   * @param time   milisseconds in which the thread will be executed
   */
  public static void schedule(Runnable thread, int time){
    threads.schedule(thread, time, TimeUnit.MILLISECONDS);
  }

  /**
   * checks if a database.ser exists in the directory storage/peer{id} loading or creating a new instance of Database
   */
  private static void loadDatabase(){
    try{
      File file = new File("storage/peer" + id + "/database.ser");
      if(file.exists()){
        FileInputStream fileStream = new FileInputStream("storage/peer" + id + "/database.ser");
        ObjectInputStream objectStream = new ObjectInputStream(fileStream);
        database = (Database) objectStream.readObject();
        objectStream.close();
        fileStream.close();
      }
      else
        database = new Database(100000000);
    } catch (IOException | ClassNotFoundException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  /**
   * saves the current instance of database to storage/peer{id}/database.ser
   */
  private static void saveDatabase(){
    try{
      File file = new File("storage/peer" + id + "/database.ser");
      if(!file.exists()){
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      FileOutputStream fileStream = new FileOutputStream("storage/peer" + id + "/database.ser");
      ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
      objectStream.writeObject(database);
      objectStream.close();
      fileStream.close();
    } catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  /**
   * channel getter
   * @param  channel channel type (MC, MDB, MDR)
   * @return multicast channel chosen
   */
  public static Channel getChannel(Utils.Channel channel){
    switch (channel) {
      case MC:
        return mc;
      case MDB:
        return mdb;
      case MDR:
        return mdr;
      default:
        return null;
    }
  }

  /**
   * id getter
   * @return peer's id
   */
  public static int getID(){
    return id;
  }

  /**
   * database getter
   * @return peer's database
   */
  public static Database getDatabase(){
    return database;
  }
}
