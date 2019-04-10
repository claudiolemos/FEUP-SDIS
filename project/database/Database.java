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
  private ConcurrentHashMap<String, Chunk> restored;
  private ConcurrentHashMap<String, Boolean> sentChunks;
  private ConcurrentHashMap<String, Boolean> wantedChunks;

  public Database(int availableSpace){
    this.availableSpace = availableSpace;
    this.usedSpace = 0;
    this.files = new ConcurrentHashMap<>();
    this.backup = new ConcurrentHashMap<>();
    this.replicationDegrees = new ConcurrentHashMap<>();
    this.restored = new ConcurrentHashMap<>();
    this.sentChunks = new ConcurrentHashMap<>();
    this.wantedChunks = new ConcurrentHashMap<>();
  }

  public void addFile(String filepath, Data file){
    if(!files.containsKey(filepath))
      files.put(filepath, file);
  }

  public boolean hasFile(String filepath){
    return files.containsKey(filepath);
  }

  public boolean hasChunk(String id){
    return backup.containsKey(id);
  }

  public void addReplicationDegree(String id, int replicationDegree){
    if(!replicationDegrees.containsKey(id))
      replicationDegrees.put(id, replicationDegree);
  }

  public void addSentChunk(String id, Boolean bool){
      sentChunks.put(id, bool);
  }

  public void addWantedChunk(String id, Boolean bool){
      wantedChunks.put(id, bool);
  }

  public boolean hasSentChunk(String id){
    if(!sentChunks.containsKey(id))
      return false;
    else
      return sentChunks.get(id);
  }

  public boolean needsWantedChunk(String id){
      return wantedChunks.containsKey(id);
  }

  public boolean hasWantedChunk(String id){
      return wantedChunks.get(id);
  }

  public void addRestoredChunk(String id, Chunk chunk){
    wantedChunks.put(id,true);
    if(!restored.containsKey(id))
      restored.put(id, chunk);
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

  public ConcurrentHashMap<String, Chunk> getRestoredChunks(){
    return restored;
  }

  public ConcurrentHashMap<String, Data> getFiles(){
    return files;
  }

  public Data getFile(String filepath){
    return files.get(filepath);
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
