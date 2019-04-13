package utils;

import java.security.MessageDigest;
import java.io.File;

/**
 * classs used for useful functions
 */
public final class Utils {

  /**
   * enum for the types of multicast channels
   */
  public enum Channel {MC, MDB, MDR}

  /**
   * hashes a message using sha256 encryption
   * @param  message content being encrypted
   * @return hashed message
   */
  public static String sha256(String message) {
      try{
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] hash = digest.digest(message.getBytes("UTF-8"));
          StringBuffer hexString = new StringBuffer();

          for (int i = 0; i < hash.length; i++) {
              String hex = Integer.toHexString(0xff & hash[i]);
              if(hex.length() == 1) hexString.append('0');
              hexString.append(hex);
          }

          return hexString.toString();
      } catch(Exception e){
          System.err.println(e.toString());
          e.printStackTrace();
          throw new RuntimeException(e);
      }
  }

  /**
   * concatenates two byte arrays
   * @param  first  the first byte array
   * @param  second the second byte array
   * @return byte array with first followed by second
   */
  public static byte[] concatenate(byte[] first, byte[] second){
     byte[] result = new byte[first.length + second.length];
     System.arraycopy(first, 0, result, 0, first.length);
     System.arraycopy(second, 0, result, first.length, second.length);
     return result;
  }

  /**
   * gets the id of a chunk
   * @param  fileID file id
   * @param  number chunk number
   * @return the chunk id
   */
  public static String getChunkID(String fileID, int number){
    return fileID + number;
  }

  /**
   * gets a file id
   * @param  filepath file's path
   * @return the file id
   */
  public static String getFileID(String filepath){
    File file = new File(filepath);
    return Utils.sha256(file.getName() + ':' + file.lastModified() + ':' + connection.Peer.getID());
  }
}
