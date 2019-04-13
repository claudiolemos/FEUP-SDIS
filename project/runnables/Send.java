package runnables;

import utils.Utils;

/**
 * class used to send messages to the multicast channels
 */
public class Send implements Runnable {

  /**
   * message being sent
   */
  private byte[] message;
  /**
   * channel to which the message is going to be sent (MC, MDB, MDR)
   */
  private Utils.Channel channel;

  /**
   * Send constructor
   * @param message content of the message being sent
   * @param channel channel to which the message is going to be sent (MC, MDB, MDR)
   */
  public Send(byte[] message, Utils.Channel channel) {
    this.message = message;
    this.channel = channel;
  }

  /**
   * when a new Send thread is executed, it sends the message to the correct channel
   */
  public void run() {
    connection.Peer.getChannel(channel).send(message);
  }
}
