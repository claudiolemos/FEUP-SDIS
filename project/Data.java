import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

class Data implements Serializable{

  private String id;
  private int replicationDegree;
  private File file;
  private ArrayList<Chunk> chunks = new ArrayList<>();

  public Data(String path, int degree){
    file = new File(path);
    replicationDegree = degree;
    split();
    // id = sha256("name" + ':' + "modified" + ':' + "owner");
  }

  private void split(){
    byte[] buffer = new byte[64000];

    try{
    FileInputStream fileStream = new FileInputStream(file);
    BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);

    int size, chunkNumber = 1;
    while((size = bufferedStream.read(buffer)) > 0){
      byte[] chunkBody = Arrays.copyOf(buffer, size);
      chunks.add(new Chunk(chunkNumber++, chunkBody, size));
    }

    if(file.length() % 64000 == 0)
      chunks.add(new Chunk(chunkNumber++, null, 0));
    }
    catch (IOException e) {

    }

  }

}
