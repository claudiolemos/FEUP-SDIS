package threads;

import utils.Utils;

public class SendMessage implements Runnable {

  private byte[] message;
  private Utils.Channel channel;

  public SendMessage(byte[] message, Utils.Channel channel) {
    this.message = message;
    this.channel = channel;
  }

  public void run() {
    connection.Peer.getChannel(channel).send(message);
  }
}
