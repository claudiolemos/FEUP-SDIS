package runnables;

import java.util.Arrays;
import java.lang.InterruptedException;
import java.util.concurrent.ThreadLocalRandom;

import database.Chunk;
import utils.Utils;

public class Receive implements Runnable {

  byte[] header;
  byte[] body;
  int senderID, chunkNumber, replicationDegree;
  String action, fileID;
  double version;

  public Receive(byte[] message) {
    for(int i = 0; i < message.length - 3; i++)
      if(message[i] == '\r' && message[i+1] == '\n' && message[i+2] == '\r' && message[i+3] == '\n'){
        this.header = Arrays.copyOfRange(message, 0, i);
        this.body = Arrays.copyOfRange(message, i+4, message.length);
        break;
      }
    parseHeader();
  }

  public void run() {
    switch(action){
      case "PUTCHUNK":
        putchunk();
        break;
      case "STORED":
        stored();
        break;
      case "DELETE":
        delete();
        break;
    }
  }

  private synchronized void putchunk(){
    try{
      Thread.sleep(ThreadLocalRandom.current().nextInt(0, 401));
      if(connection.Peer.getID() != senderID){
        if(connection.Peer.getDatabase().getAvailableSpace() >= body.length){
          Chunk chunk = new Chunk(fileID, chunkNumber, body, body.length);

          if(connection.Peer.getDatabase().getBackupChunks().containsKey(chunk.getID()))
            return;
          else
            connection.Peer.getDatabase().addBackupChunk(chunk.getID(), chunk);

          chunk.save();
          String header = "STORED " + version + " " + connection.Peer.getID() + " " + fileID + " " + chunkNumber + "\r\n\r\n";
          System.out.println("Sending " + header.substring(0,header.length() - 4));
          connection.Peer.execute(new Send(header.getBytes(), Utils.Channel.MC));
        }
      }
    } catch (InterruptedException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  private synchronized void stored(){
    if(connection.Peer.getID() != senderID){
      connection.Peer.getDatabase().increaseReplicationDegree(fileID + chunkNumber);
    }
  }

  private synchronized void delete(){
    if(connection.Peer.getID() != senderID){
      connection.Peer.getDatabase().deleteChunks(fileID);
    }
  }

  private void parseHeader(){
    String[] array = new String(header).trim().split(" ");
    action = array[0];
    version = Double.parseDouble(array[1]);
    senderID = Integer.parseInt(array[2]);
    fileID = array[3];
    if(!action.equals("DELETE")) chunkNumber = Integer.parseInt(array[4]);
    if(action.equals("PUTCHUNK")) replicationDegree = Integer.parseInt(array[5]);
  }
}
