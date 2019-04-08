package database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Chunk{

  private int number, size;
  private byte[] body;

  public Chunk(int number, byte[] body, int size){
    this.number = number;
    this.body = body;
    this.size = size;
  }

	public int getNumber() {
		return number;
	}

	public int getSize() {
		return size;
	}

	public byte[] getBody() {
		return body;
	}

  public void save(String path){
    try{
      File file = new File(path);
      if(!file.exists()){
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      FileOutputStream fileStream = new FileOutputStream(path);
      fileStream.write(body);
    } catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }
}
