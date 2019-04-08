package database;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Database implements Serializable{

  private int availableSpace;
  private ConcurrentHashMap<String, Chunk> backup;
  private ConcurrentHashMap<String, Chunk> restored;

  public Database(){
    this.availableSpace = 100000000;
    this.backup = new ConcurrentHashMap<>();
    this.restored = new ConcurrentHashMap<>();
  }

  public void addBackupChunk(String key, Chunk chunk){
    backup.put(key, chunk);
    availableSpace -= chunk.getSize();
  }

  public ConcurrentHashMap<String, Chunk> getBackupChunks(){
    return backup;
  }

  public ConcurrentHashMap<String, Chunk> getRestoredChunks(){
    return restored;
  }

  public int getAvailableSpace(){
    return availableSpace;
  }
}
