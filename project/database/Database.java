package database;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Database implements Serializable{

  private int capacity;
  private ConcurrentHashMap<String, Chunk> chunks;

  public Database(){
    this.capacity = 100000;
    this.chunks = new ConcurrentHashMap<>();
  }
}
