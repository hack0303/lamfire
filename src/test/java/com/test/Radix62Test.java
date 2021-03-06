package com.test;

import com.lamfire.code.MurmurHash;
import com.lamfire.code.Radixes;
import com.lamfire.utils.Bytes;

public class Radix62Test {

	public static void radix62() {
		for(int a=0;a<9999;a++){
			long v = MurmurHash.hash64(Bytes.toBytes(a), 1313);
			System.out.println( v + " -> " +Radixes.digest32(v));
		}
	}
	
	public static void main(String[] args) {
		int radix = 37;
		long number = 3720368l;
		String digest = Radixes.encode(number,radix);
		
		System.out.println(String.format("[EN] %d - %d -> %s", radix,number,digest));
		System.out.println(String.format("[DE] %d - %s -> %d", radix,digest,Radixes.decode(digest,radix)));
		
		System.out.println(String.format("[DE] %d - %s -> %d", radix,digest.toUpperCase(),Radixes.decode(digest.toUpperCase(),radix)));
	}

}
