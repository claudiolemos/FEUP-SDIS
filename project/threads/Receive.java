package threads;

import java.util.Arrays;

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
    if(connection.Peer.getID() != senderID){
      System.out.println("action :" + action + ":");
      System.out.println("version :" + version + ":");
      System.out.println("senderID :" + senderID + ":");
      System.out.println("fileID :" + fileID + ":");
      System.out.println("chunkNumber :" + chunkNumber + ":");
      System.out.println("replicationDegree :" + replicationDegree + ":");
    }
  }

  private void parseHeader(){
    String[] array = (new String(header)).trim().split(" ");
    action = array[0];
    version = Double.parseDouble(array[1]);
    senderID = Integer.parseInt(array[2]);
    fileID = array[3];
    if(!action.equals("DELETE")) chunkNumber = Integer.parseInt(array[4]);
    if(action.equals("PUTCHUNK")) replicationDegree = Integer.parseInt(array[5]);
  }
}
