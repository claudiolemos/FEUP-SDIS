package runnables;

import utils.Utils;

public class Send implements Runnable {

  private byte[] message;
  private Utils.Channel channel;

  public Send(byte[] message, Utils.Channel channel) {
    this.message = message;
    this.channel = channel;
  }

  public void run() {
    connection.Peer.getChannel(channel).send(message);
  }
}
