package database;

import java.io.Serializable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * class used to store a file's chunk info
 */
public class Chunk  implements Serializable{

  /**
   * id of the file to which the chunk belongs
   */
  private String fileID;
  /**
   * position of the chunk in the file
   */
  private int number;
  /**
   * chunk's content size
   */
  private int size;
  /**
   * desired replication degree of the chunk
   */
  private int replicationDegree;
  /**
   * chunk's content body
   */
  private byte[] body;

  /**
   * Chunk constructor
   * @param fileID id of the file to which the chunk belongs
   * @param number position of the chunk in the file
   * @param body   chunk's content body
   * @param size   chunk's content size
   */
  public Chunk(String fileID, int number, byte[] body, int size){
    this.fileID = fileID;
    this.number = number;
    this.body = body;
    this.size = size;
  }

  /**
   * Chunk constructor with replication degree added
   * @param fileID            id of the file to which the chunk belongs
   * @param number            position of the chunk in the file
   * @param body              chunk's content body
   * @param size              chunk's content size
   * @param replicationDegree desired replication degree of the chunk
   */
  public Chunk(String fileID, int number, byte[] body, int size, int replicationDegree){
    this(fileID, number, body, size);
    this.replicationDegree = replicationDegree;
  }

  public void save(){
    try{
      File file = new File("storage/peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + number);
      if(!file.exists()){
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      FileOutputStream fileStream = new FileOutputStream("storage/peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + number);
      fileStream.write(body);
    } catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  public void delete(){
    File file = new File("storage/peer" + connection.Peer.getID() + "/backup/" + fileID + "/" + number);
    file.delete();
  }

  /**
   * file id getter
   * @return chunk's file id
   */
  public String getFileID(){
    return fileID;
  }

  /**
   * number getter
   * @return chunk's number
   */
	public int getNumber() {
		return number;
	}

  /**
   * id getter
   * @return chunk's id
   */
  public String getID(){
    return fileID + number;
  }

  /**
   * size getter
   * @return chunk's size
   */
	public int getSize() {
		return size;
	}

  /**
   * replication degree getter
   * @return chunk's replication degree
   */
	public int getReplicationDegree() {
		return replicationDegree;
	}

  /**
   * body getter
   * @return chunk's body getter
   */
	public byte[] getBody() {
		return body;
	}
}
