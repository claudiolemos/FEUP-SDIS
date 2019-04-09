package database;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.util.Map;
import java.util.Iterator;

public class Database implements Serializable{

  private int availableSpace, usedSpace;
  private ConcurrentHashMap<String, Data> files;
  private ConcurrentHashMap<String, Chunk> backup;
  private ConcurrentHashMap<String, Integer> replicationDegrees;

  public Database(int availableSpace){
    this.availableSpace = availableSpace;
    this.usedSpace = 0;
    this.files = new ConcurrentHashMap<>();
    this.backup = new ConcurrentHashMap<>();
    this.replicationDegrees = new ConcurrentHashMap<>();
  }

  public void addFile(String filepath, Data file){
    if(!files.containsKey(filepath))
      files.put(filepath, file);
  }

  public boolean hasFile(String filepath){
    return files.containsKey(filepath);
  }

  public void addReplicationDegree(String id, int replicationDegree){
    if(!replicationDegrees.containsKey(id))
      replicationDegrees.put(id, replicationDegree);
  }

  public void increaseReplicationDegree(String id){
    if(!replicationDegrees.containsKey(id))
      replicationDegrees.put(id, 1);
    else
      replicationDegrees.put(id, replicationDegrees.get(id)+1);
  }

  public int getReplicationDegree(String id){
    return replicationDegrees.get(id);
  }

  public void addBackupChunk(String id, Chunk chunk){
    backup.put(id, chunk);
    increaseReplicationDegree(id);
    availableSpace -= chunk.getSize();
    usedSpace += chunk.getSize();
  }

  public ConcurrentHashMap<String, Chunk> getBackupChunks(){
    return backup;
  }

  public ConcurrentHashMap<String, Data> getFiles(){
    return files;
  }

  public void deleteChunks(String fileID){
    for(Iterator<Map.Entry<String, Chunk>> iterator = backup.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry<String, Chunk> entry = iterator.next();
      Chunk chunk = entry.getValue();
      if(chunk.getFileID().equals(fileID)) {
        chunk.delete();
        replicationDegrees.remove(chunk.getID());
        availableSpace += chunk.getSize();
        iterator.remove();
      }
    }
  }

  public int getAvailableSpace(){
    return availableSpace;
  }

  public int getUsedSpace(){
    return usedSpace;
  }
}
