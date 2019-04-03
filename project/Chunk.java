public class Chunk{

  private int number, size;
  private byte[] body;

  public Chunk(int number, byte[] body, int size){
    this.number = number;
    this.body = body;
    this.size = size;
  }
}
