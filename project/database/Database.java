package database;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.util.Map;
import java.util.Iterator;

import utils.Utils;

/**
 * class used to represent a peer's database
 */
public class Database implements Serializable{

  /**
   * current available space
   */
  private int availableSpace;
  /**
   * current used space
   */
  private int usedSpace;
  /**
   * identifiers of the files sent by the peer to be backedup
   */
  private ConcurrentHashMap<String, Data> files;
  /**
   * chunks being currently backedup by the peer
   */
  private ConcurrentHashMap<String, Chunk> backup;
  /**
   * replication degrees of the chunks received and sent by the peer
   */
  private ConcurrentHashMap<String, Integer> replicationDegrees;
  /**
   * chunks received by the peer to restore a previously sent file
   */
  private ConcurrentHashMap<String, Chunk> restored;
  /**
   * id of the chunks sent by every peer
   */
  private ConcurrentHashMap<String, Boolean> sentChunks;
  /**
   * id of the chunks received by the peer
   */
  private ConcurrentHashMap<String, Boolean> receivedChunks;
  /**
   * id of the chunks needed to restore a file
   */
  private ConcurrentHashMap<String, Boolean> wantedChunks;

  /**
   * Database constructor
   * @param availableSpace initial available space
   */
  public Database(int availableSpace){
    this.availableSpace = availableSpace;
    this.usedSpace = 0;
    this.files = new ConcurrentHashMap<>();
    this.backup = new ConcurrentHashMap<>();
    this.replicationDegrees = new ConcurrentHashMap<>();
    this.restored = new ConcurrentHashMap<>();
    this.sentChunks = new ConcurrentHashMap<>();
    this.receivedChunks = new ConcurrentHashMap<>();
    this.wantedChunks = new ConcurrentHashMap<>();
  }

  /**
   * adds a file to the database
   * @param filepath file's path
   * @param file     file's data
   */
  public void addFile(String filepath, Data file){
    if(!files.containsKey(Utils.getFileID(filepath)))
      files.put(Utils.getFileID(filepath), file);
  }

  /**
   * deletes a file from the database
   * @param filepath file's path
   */
  public void deleteFile(String filepath){
    if(files.containsKey(Utils.getFileID(filepath)))
      files.remove(Utils.getFileID(filepath));
  }

  /**
   * checks if the database has a file
   * @param  filepath file's path
   * @return boolean on wether or not the database has the specified file
   */
  public boolean hasFile(String filepath){
    return files.containsKey(Utils.getFileID(filepath));
  }

  /**
   * checks if the database has a chunk
   * @param  id chunk id
   * @return boolean on wether or not the database has the specified chunk
   */
  public boolean hasChunk(String id){
    return backup.containsKey(id);
  }

  /**
   * adds a chunk with a specified replication degree to the database
   * @param id                chunk id
   * @param replicationDegree specified replication degree
   */
  public void addReplicationDegree(String id, int replicationDegree){
    if(!replicationDegrees.containsKey(id))
      replicationDegrees.put(id, replicationDegree);
  }

  /**
   * adds a sent chunk to the database
   * @param id   chunk id
   * @param bool boolean on wether or not the chunk has been sent by any peer
   */
  public void addSentChunk(String id, Boolean bool){
    sentChunks.put(id, bool);
  }

  /**
   * adds a wanted chunk to the database
   * @param id   chunk id
   * @param bool boolean on wether or not the chunk has been received by the peer that wants it
   */
  public void addWantedChunk(String id, Boolean bool){
    wantedChunks.put(id, bool);
  }

  /**
   * adds a received chunk to the database
   * @param id   chunk id
   * @param bool boolean on wether or not the chunk has been received by any peer
   */
  public void addReceivedChunk(String id, Boolean bool){
    receivedChunks.put(id, bool);
  }

  /**
   * checks if the database has sent a chunk
   * @param  id chunk id
   * @return boolean on wether or not the chunk has been sent by any peer
   */
  public boolean hasSentChunk(String id){
    if(!sentChunks.containsKey(id))
      return false;
    else
      return sentChunks.get(id);
  }

  /**
   * checks if the database has received a chunk
   * @param  id chunk id
   * @return boolean on wether or not the chunk has been received by any peer
   */
  public boolean hasReceivedChunk(String id){
    if(!receivedChunks.containsKey(id))
      return false;
    else
      return receivedChunks.get(id);
  }

  /**
   * checks if a chunk is needed by the peer
   * @param  id chunkid
   * @return boolean on wether or not the chunk is needed
   */
  public boolean needsWantedChunk(String id){
      return wantedChunks.containsKey(id);
  }

  /**
   * checks if a peer has a wanted a chunk
   * @param  id chunkid
   * @return
   */
  public boolean hasWantedChunk(String id){
      return wantedChunks.get(id);
  }

  /**
   * adds a restored chunk to the database
   * @param id    chunk id
   * @param chunk chunk's data
   */
  public void addRestoredChunk(String id, Chunk chunk){
    wantedChunks.put(id,true);
    if(!restored.containsKey(id))
      restored.put(id, chunk);
  }

  /**
   * increases a chunk replication degree
   * @param id chunk id
   */
  public void increaseReplicationDegree(String id){
    if(!replicationDegrees.containsKey(id))
      replicationDegrees.put(id, 1);
    else
      replicationDegrees.put(id, replicationDegrees.get(id)+1);
  }

  /**
   * decreases a chunk replication degree
   * @param id chunk id
   */
  public void decreaseReplicationDegree(String id){
    if(replicationDegrees.containsKey(id))
      replicationDegrees.put(id, replicationDegrees.get(id)-1);
  }

  /**
   * adds a backedup chunk to the database
   * @param id    chunk id
   * @param chunk chunk data
   */
  public void addBackupChunk(String id, Chunk chunk){
    backup.put(id, chunk);
    increaseReplicationDegree(id);
    availableSpace -= chunk.getSize();
    usedSpace += chunk.getSize();
  }

  /**
   * removes a backedup chunk from the database
   * @param id    chunk id
   * @param chunk chunk data
   */
  public void removeBackupChunk(String id, Chunk chunk){
    backup.remove(id);
    decreaseReplicationDegree(id);
    availableSpace += chunk.getSize();
    usedSpace -= chunk.getSize();
  }

  /**
   * deletes all backedup chunks from a specified file
   * @param fileID file id
   */
  public void deleteChunks(String fileID){
    boolean deleted = false;

    for(Iterator<Map.Entry<String, Chunk>> iterator = backup.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry<String, Chunk> entry = iterator.next();
      Chunk chunk = entry.getValue();
      if(chunk.getFileID().equals(fileID)) {
        deleted = true;
        chunk.delete();
        replicationDegrees.remove(chunk.getID());
        availableSpace += chunk.getSize();
        usedSpace -= chunk.getSize();
        iterator.remove();
      }
    }

    if(deleted) System.out.println("File " + fileID + " was deleted from database.\n");
  }

  /**
   * available space getter
   * @return database's available space
   */
  public int getAvailableSpace(){
    return availableSpace;
  }

  /**
   * available space setter
   * @param availableSpace new available space
   */
  public void setAvailableSpace(int availableSpace){
    this.availableSpace = availableSpace;
  }

  /**
   * used space getter
   * @return database's used space
   */
  public int getUsedSpace(){
    return usedSpace;
  }

  /**
   * storage space getter
   * @return database's storage space
   */
  public int getSpace(){
    return usedSpace + availableSpace;
  }

  /**
   * backup getter
   * @return database's backedup chunks
   */
  public ConcurrentHashMap<String, Chunk> getBackupChunks(){
    return backup;
  }

  /**
   * restored getter
   * @return database's restored chunks
   */
  public ConcurrentHashMap<String, Chunk> getRestoredChunks(){
    return restored;
  }

  /**
   * files getter
   * @return database's files
   */
  public ConcurrentHashMap<String, Data> getFiles(){
    return files;
  }

  /**
   * file getter
   * @param  filepath file's path
   * @return specified file
   */
  public Data getFile(String filepath){
    return files.get(Utils.getFileID(filepath));
  }

  /**
   * replication degree getter
   * @param  id chunk id
   * @return specified chunk's replication degree
   */
  public int getReplicationDegree(String id){
    if(replicationDegrees.containsKey(id))
      return replicationDegrees.get(id);
    else
      return 0;
  }
}
