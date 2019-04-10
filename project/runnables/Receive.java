package runnables;

import java.util.Arrays;
import java.lang.InterruptedException;
import java.util.concurrent.ThreadLocalRandom;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import database.Chunk;
import utils.Utils;

public class Receive implements Runnable {

  byte[] header;
  byte[] body;
  int senderID, chunkNumber, replicationDegree;
  String action, fileID;
  double version;

  public Receive(byte[] message) {
    for(int i = 0; i < message.length - 4 + 1; i++)
      if(message[i] == '\r' && message[i+1] == '\n' && message[i+2] == '\r' && message[i+3] == '\n'){
        this.header = Arrays.copyOfRange(message, 0, i);
        if(i+4 != message.length) this.body = Arrays.copyOfRange(message, i+4, message.length);
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
      case "GETCHUNK":
        getchunk();
        break;
      case "CHUNK":
        chunk();
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

          String header = "STORED " + version + " " + connection.Peer.getID() + " " + fileID + " " + chunkNumber + "\r\n\r\n";
          System.out.println("Sending " + header.substring(0,header.length() - 4));
          connection.Peer.execute(new Send(header.getBytes(), Utils.Channel.MC));
          chunk.save();
        }
      }
    } catch (InterruptedException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  private synchronized void stored(){
    if(connection.Peer.getID() != senderID)
      connection.Peer.getDatabase().increaseReplicationDegree(Utils.getChunkID(fileID, chunkNumber));
  }

  private synchronized void delete(){
    if(connection.Peer.getID() != senderID)
      connection.Peer.getDatabase().deleteChunks(fileID);
  }

  private synchronized void getchunk(){
    try{
      Thread.sleep(ThreadLocalRandom.current().nextInt(0, 401));

      if(connection.Peer.getID() != senderID && connection.Peer.getDatabase().hasChunk(Utils.getChunkID(fileID, chunkNumber)) && !connection.Peer.getDatabase().hasSentChunk(Utils.getChunkID(fileID, chunkNumber))){
        connection.Peer.getDatabase().addSentChunk(Utils.getChunkID(fileID, chunkNumber), true);
        File file = new File("storage/peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + chunkNumber);
        byte[] buffer = new byte[(int)file.length()];
        FileInputStream fileStream = new FileInputStream(file);
        BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);
        int size = bufferedStream.read(buffer);
        String header = "CHUNK " + version + " " + connection.Peer.getID() + " " + fileID + " " + chunkNumber + "\r\n\r\n";
        System.out.println("Sending " + header.substring(0,header.length() - 4));
        byte[] message = Utils.concatenate(header.getBytes(), Arrays.copyOf(buffer, size));
        connection.Peer.execute(new Send(message, Utils.Channel.MDR));
      }
    } catch (InterruptedException | IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  private synchronized void chunk(){
    if(connection.Peer.getID() != senderID){
      if(connection.Peer.getDatabase().needsWantedChunk(Utils.getChunkID(fileID, chunkNumber)))
        connection.Peer.getDatabase().addRestoredChunk(Utils.getChunkID(fileID, chunkNumber), new Chunk(fileID, chunkNumber, body, body.length));
      else
        connection.Peer.getDatabase().addSentChunk(Utils.getChunkID(fileID, chunkNumber), true);
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
