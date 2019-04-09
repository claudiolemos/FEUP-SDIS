package database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Chunk{

  private String fileID;
  private int number, size;
  private byte[] body;

  public Chunk(String fileID, int number, byte[] body, int size){
    this.fileID = fileID;
    this.number = number;
    this.body = body;
    this.size = size;
  }

  public void save(){
    try{
      File file = new File("peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + number);
      if(!file.exists()){
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      FileOutputStream fileStream = new FileOutputStream("peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + number);
      fileStream.write(body);
    } catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  public void delete(){
    File file = new File("peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + number);
    file.delete();
  }

  public String getFileID(){
    return fileID;
  }

	public int getNumber() {
		return number;
	}

  public String getID(){
    return fileID + number;
  }

	public int getSize() {
		return size;
	}

	public byte[] getBody() {
		return body;
	}
}
