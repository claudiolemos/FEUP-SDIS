import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

public class Data implements Serializable{

  private String id;
  private int replicationDegree;
  private File file;
  private ArrayList<Chunk> chunks = new ArrayList<>();

  public Data(String path, int replicationDegree){
    this.file = new File(path);
    this.replicationDegree = replicationDegree;
    createChunks();
    this.id = Utils.sha256(this.file.getName() + ':' + this.file.lastModified() + ':' + this.file.getAbsoluteFile().getParent());
  }

  private void createChunks(){
    byte[] buffer = new byte[64000];

    try{
    FileInputStream fileStream = new FileInputStream(file);
    BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);

    int size, chunkNumber = 1;
    while((size = bufferedStream.read(buffer)) != -1)
      chunks.add(new Chunk(chunkNumber++, Arrays.copyOf(buffer, size), size));

    if(file.length() % 64000 == 0)
      chunks.add(new Chunk(chunkNumber++, null, 0));
    }
    catch (IOException e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  public ArrayList<Chunk> getChunks(){
    return chunks;
  }

  public String getID(){
    return id;
  }

}
