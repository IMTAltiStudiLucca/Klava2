package common;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeneralMethods
{
    public static String getCurrrentDatetimeAsString()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = new Date();
        return dateFormat.format(date);
    }
        
    public static String concat(String ...strings)
    {
        StringBuilder  sb = new StringBuilder(strings.length);
        for(int i =0; i < strings.length; i++)
        {
        sb.append(strings[i]);
        }
        return sb.toString();
    }
   
    
	public static String integerToHashString(MessageDigest mdEnc, int number) {
/*		String str = String.valueOf(i);
		mdEnc.update(str.getBytes(), 0, str.length());
		String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
		return md5;*/
		
		String str = String.valueOf(number);
		mdEnc.update(str.getBytes(), 0, str.length());
	//	String ms = mdEnc.toString();
		//String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
		byte[] hash = mdEnc.digest();
		StringBuffer md5 = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
		    if ((0xff & hash[i]) < 0x10) {
		    	md5.append("0"
		                + Integer.toHexString((0xFF & hash[i])));
		    } else {
		    	md5.append(Integer.toHexString(0xFF & hash[i]));
		    }
		}
		String md5Value = md5.toString();
		return md5Value;
	}
}