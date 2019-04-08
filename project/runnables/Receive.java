package runnables;

import java.util.Arrays;

import database.Chunk;
import utils.Utils;

public class Receive implements Runnable {

  byte[] header;
  byte[] body;
  int senderID, chunkNumber, replicationDegree;
  String action, fileID;
  double version;

  public Receive(byte[] message) {
    for(int i = 0; i < message.length - 4; i++)
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
    }
  }

  private synchronized void putchunk(){
    if(connection.Peer.getID() != senderID){
      if(connection.Peer.getDatabase().getAvailableSpace() >= body.length){
        Chunk chunk = new Chunk(chunkNumber, body, body.length);

        if(connection.Peer.getDatabase().getBackupChunks().containsKey(fileID + chunkNumber))
          return;
        else
          connection.Peer.getDatabase().addBackupChunk(fileID + chunkNumber, chunk);

        chunk.save("peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + chunkNumber);
      }
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
