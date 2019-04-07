package file;

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
}
