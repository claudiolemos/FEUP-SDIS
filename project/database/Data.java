package database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import utils.Utils;

/**
 * class used to store a file's data
 */
public class Data implements Serializable{

  /**
   * file's unique id created using the sha256's hash of the file's name, last modified date and the peer to which it belongs, all seperated by a colon
   */
  private String id;
  /**
   * desired replication degree
   */
  private int replicationDegree;
  /**
   * file
   */
  private File file;
  /**
   * array list of all the file's chunk
   */
  private ArrayList<Chunk> chunks = new ArrayList<>();

  /**
   * Data constructor
   * @param path              path to the file
   * @param replicationDegree desired replication degree
   */
  public Data(String path, int replicationDegree){
    this.file = new File(path);
    this.replicationDegree = replicationDegree;
    this.id = Utils.sha256(this.file.getName() + ':' + this.file.lastModified() + ':' + connection.Peer.getID());
    createChunks();
  }

  /**
   * reads the file (64kb at a time) and creates the chunks
   */
  private void createChunks(){
    byte[] buffer = new byte[64000];

    try{
    FileInputStream fileStream = new FileInputStream(file);
    BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);

    int size, chunkNumber = 1;
    while((size = bufferedStream.read(buffer)) != -1)
      chunks.add(new Chunk(id, chunkNumber++, Arrays.copyOf(buffer, size), size, replicationDegree));

    if(file.length() % 64000 == 0)
      chunks.add(new Chunk(id, chunkNumber++, null, 0, replicationDegree));
    }
    catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  /**
   * chunks getter
   * @return file's chunks
   */
  public ArrayList<Chunk> getChunks(){
    return chunks;
  }

  /**
   * id getter
   * @return file's id
   */
  public String getID(){
    return id;
  }

  /**
   * path getter
   * @return file's path
   */
  public String getPath(){
    return file.getPath();
  }

  /**
   * replication degree getter
   * @return file's replication degree
   */
  public int getReplicationDegree(){
    return replicationDegree;
  }

}
