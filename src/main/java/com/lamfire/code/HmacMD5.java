package com.lamfire.code;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HmacMD5 {
	private static final String HMAC_MD5 = "HmacMD5";


	public static byte[] digest(byte[] source,byte[] key) throws  Exception{
		SecretKey sk  = new SecretKeySpec(key, HMAC_MD5);
		Mac mac = Mac.getInstance(HMAC_MD5);
		mac.init(sk);
		byte[] result = mac.doFinal(source);
		return result;
	}

	public static String hash(byte[] source,byte[] key) throws  Exception{
		byte[] bytes = digest(source,key);
		return Hex.encode(bytes);
	}
	
	public static String hash(String source,String key)throws  Exception{
		byte[] bytes = digest(source.getBytes(),key.getBytes());
		return Hex.encode(bytes);
	}
}
