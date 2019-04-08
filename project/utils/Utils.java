package utils;

import java.security.MessageDigest;

public final class Utils {
  public enum Channel {MC, MDB, MDR}

  public static String sha256(String base) {
      try{
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] hash = digest.digest(base.getBytes("UTF-8"));
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

  public static byte[] concatenate(byte[] first, byte[] second){
     byte[] result = new byte[first.length + second.length];
     System.arraycopy(first, 0, result, 0, first.length);
     System.arraycopy(second, 0, result, first.length, second.length);
     return result;
  }
}
