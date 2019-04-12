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

import database.*;
import runnables.*;
import utils.Utils;

public class Peer implements RMI{

  private static double version;
  private static int id, mcPort, mdbPort, mdrPort;
  private static String accessPoint, mcIP, mdbIP, mdrIP;
  private static ScheduledThreadPoolExecutor threads;
  private static Channel mc, mdb, mdr;
  private static Database database;

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
      System.out.println("Peer " + id + " ready");
    } catch (Exception e) {
			System.err.println("Peer exception: " + e.toString());
			e.printStackTrace();
    }

    loadDatabase();
    execute(mc);
    execute(mdb);
    execute(mdr);

    // Runtime.getRuntime().addShutdownHook(new Thread(Peer::saveDatabase));
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

  public synchronized void backup(String filepath, int replicationDegree){
    Data file = new Data(filepath, replicationDegree);
    database.addFile(filepath, file);

    for(int i = 0; i < file.getChunks().size(); i++){
      try{
        Chunk chunk = file.getChunks().get(i);
        String header = "PUTCHUNK " + version + " " + id + " " + file.getID() + " " + chunk.getNumber() + " " + replicationDegree + "\r\n\r\n";
        System.out.println("Sending " + header.substring(0,header.length() - 4));
        byte[] message = Utils.concatenate(header.getBytes(), chunk.getBody());
        database.addReplicationDegree(chunk.getID(),0);

        int counter = 1, timer = 1000;

        do {
          System.out.println("PUTCHUNK Try #" + counter);
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

  public synchronized void delete(String filepath){
    if(database.hasFile(filepath)){
      String header = "DELETE " + version + " " + id + " " + database.getFile(filepath).getID() + "\r\n\r\n";
      database.deleteFile(filepath);
      System.out.println("Sending " + header.substring(0,header.length() - 4));
      execute(new Send(header.getBytes(), Utils.Channel.MC));
    }
  }

  public synchronized void restore(String filepath){
    if(database.hasFile(filepath)){
      for(int i = 0; i < database.getFile(filepath).getChunks().size(); i++){
        try{
          database.addWantedChunk(Utils.getChunkID(database.getFile(filepath).getID(),database.getFile(filepath).getChunks().get(i).getNumber()),false);
          String header = "GETCHUNK " + version + " " + id + " " + database.getFile(filepath).getID() + " " + database.getFile(filepath).getChunks().get(i).getNumber() + "\r\n\r\n";
          System.out.println("Sending " + header.substring(0,header.length() - 4));

          int counter = 1, timer = 1000;

          do {
            System.out.println("GETCHUNK Try #" + counter);
            execute(new Send(header.getBytes(), Utils.Channel.MC));
            Thread.sleep(timer);
            timer *= 2;
            counter++;
          } while (!database.hasWantedChunk(Utils.getChunkID(database.getFile(filepath).getID(),database.getFile(filepath).getChunks().get(i).getNumber())) && counter < 6);
        }catch (InterruptedException e) {
          System.err.println(e.toString());
          e.printStackTrace();
        }
      }

      try{
        File file = new File("storage/peer" + getID() + "/restored/" + Paths.get(filepath).getFileName().toString());
        if(!file.exists()){
          file.getParentFile().mkdirs();
          file.createNewFile();
        }
        FileOutputStream fileStream = new FileOutputStream("storage/peer" + getID() + "/restored/" + Paths.get(filepath).getFileName().toString());

        for(int i = 0; i < database.getFile(filepath).getChunks().size(); i++)
            fileStream.write(database.getRestoredChunks().get(Utils.getChunkID(database.getFile(filepath).getID(),database.getFile(filepath).getChunks().get(i).getNumber())).getBody());
      } catch (IOException e) {
        System.err.println(e.toString());
        e.printStackTrace();
      }
    }
  }

  public synchronized void reclaim(int reclaimSpace){
    database.setAvailableSpace(reclaimSpace*1000 - database.getUsedSpace());

    if(database.getAvailableSpace() > 0)
      System.out.println("Peer " + id + " can now store up to " + database.getSpace()/1000 + "kb");
    else if(database.getAvailableSpace() == 0)
      System.out.println("Peer " + id + " is at full capacity");
    else {
      for(Iterator<Map.Entry<String, Chunk>> iterator = database.getBackupChunks().entrySet().iterator(); iterator.hasNext();){
        if(database.getAvailableSpace() < 0){
          Map.Entry<String, Chunk> entry = iterator.next();
          String chunkID = entry.getKey();
          Chunk chunk = entry.getValue();
          iterator.remove();
          database.addAvailableSpace(chunk.getSize());
          database.removeUsedSpace(chunk.getSize());
          database.decreaseReplicationDegree(chunkID);
          String header = "REMOVED " + version + " " + id + " " + chunk.getFileID() + " " + chunk.getNumber() + "\r\n\r\n";
          System.out.println("Sending " + header.substring(0,header.length() - 4));
          execute(new Send(header.getBytes(), Utils.Channel.MC));
        }
        else
          break;
      }


    }
  }

  public synchronized void state(){
    System.out.println("Peer " + id + " state:");
    printFilesInfo();
    printBackupChunksInfo();
    printStorageInfo();
  }

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

  public void printStorageInfo(){
    System.out.println("  Storage");
    System.out.println("    Used: " + database.getUsedSpace()/1000.0 + "kb");
    System.out.println("    Disk Space: "+ (database.getAvailableSpace() + database.getUsedSpace())/1000 + "kb");
  }

  public static void execute(Runnable thread){
    threads.execute(thread);
  }

  public static void schedule(Runnable thread, int time){
    threads.schedule(thread, time, TimeUnit.MILLISECONDS);
  }

  private static void loadDatabase(){
    try{
      File file = new File("database/" + id + "/database.ser");
      if(file.exists()){
        FileInputStream fileStream = new FileInputStream("database/" + id + "/database.ser");
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

  private static void saveDatabase(){
    try{
      File file = new File("database/" + id + "/database.ser");
      if(!file.exists()){
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      FileOutputStream fileStream = new FileOutputStream("database/" + id + "/database.ser");
      ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
      objectStream.writeObject(database);
      objectStream.close();
      fileStream.close();
    } catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

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

  public static int getID(){
    return id;
  }

  public static Database getDatabase(){
    return database;
  }
}
