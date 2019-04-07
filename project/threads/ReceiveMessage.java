package threads;

import utils.Utils;

public class ReceiveMessage implements Runnable {

  byte[] message;

  public ReceiveMessage(byte[] message) {
    this.message = message;
  }

  public void run() {
    System.out.println(new String(this.message, 0, this.message.length));
  }
}
