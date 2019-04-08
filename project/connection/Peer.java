package connection;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

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
      registry.bind(accessPoint, rmi);
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

    for(int i = 0; i < file.getChunks().size(); i++){
      try{
        Chunk chunk = file.getChunks().get(i);
        String header = "PUTCHUNK " + version + " " + id + " " + file.getID() + " " + chunk.getNumber() + " " + replicationDegree + "\r\n\r\n";
        System.out.println("Sending " + header.substring(0,header.length() - 4));
        byte[] message = Utils.concatenate(header.getBytes("US-ASCII"), chunk.getBody());
        Send thread = new Send(message, Utils.Channel.MDB);
        execute(thread);
        Thread.sleep(500);
        // threads.schedule(new ManagePutChunkThread(message, 1, file.getId(), chunk.getNr(), replicationDegree), 1, TimeUnit.SECONDS);
      }
      catch (InterruptedException | UnsupportedEncodingException e) {
        System.err.println(e.toString());
        e.printStackTrace();
      }
    }
  }


  public void restore(String filepath){System.out.println("RESTORE" + filepath);}
  public void delete(String filepath){System.out.println("DELETE" + filepath);}
  public void reclaim(int reclaimSpace){System.out.println("RECLAIM" + reclaimSpace);}
  public void state(){System.out.println("STATE");}

  public static void execute(Runnable thread){
    threads.execute(thread);
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
        database = new Database();
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
