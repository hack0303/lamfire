package com.lamfire.code;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.lamfire.utils.IOUtils;

/**
 * RSA算法工具
 * 
 * @author lamfire
 * 
 */
public class RSA {
	/**
	 * 加密算法RSA
	 */
	public static final String KEY_ALGORITHM = "RSA";

	/**
	 * 签名算法
	 */
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";


    /**
     * 密钥长度
     */
    private int keySize = 1024;
    private byte[] privateKey;
    private byte[] publicKey;

    public RSA(int keySize, byte[] privateKey, byte[] publicKey) {
        this.keySize = keySize;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    /**
	 * 生成密钥对(公钥和私钥)
	 * @return
	 * @throws Exception
	 */
	public static KeyPair genKeyPair(int keySize) throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(keySize);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		return keyPair;
	}

	/**
	 * 用私钥对信息生成数字签名
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public String signatureAsBase64(byte[] data) throws Exception {
		PrivateKey privateK = toPrivateKey(privateKey);
		return Base64.encode(signature(data,privateK));
	}

	/**
	 * 用私钥对信息生成数字签名
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] signature(byte[] data, PrivateKey privateKey) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	/**
	 * 校验数字签名
	 * @param data
	 * @param sign
	 * @return
	 * @throws Exception
	 */
	public boolean verifySignatureAsBase64(byte[] data,String sign) throws Exception {
		return verifySignature(data,Base64.decode(sign),publicKey);
	}
	
	/**
	 * 校验数字签名
	 * @param data
	 * @param sign
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static boolean verifySignature(byte[] data, byte[] sign,byte[] publicKey) throws Exception {
		PublicKey publicK = toPublicKey(publicKey);
		return verifySignature(data,sign,publicK);
	}
	
	/**
	 * 校验数字签名
	 * @param data
	 * @param sign
	 * @param publicK
	 * @return
	 * @throws Exception
	 */
	public static boolean verifySignature(byte[] data, byte[] sign,PublicKey publicK) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicK);
		signature.update(data);
		return signature.verify(sign);
	}

	/**
	 * 私钥解密
	 * @param data
	 * @return
	 * @throws Exception
	 */
	 
	public byte[] decodeByPrivateKey(byte[] data) throws Exception {
		Key privateK = toPrivateKey(privateKey);
		return decode(data, privateK,keySize);
	}

	/**
	 * 公钥解密
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] decodeByPublicKey(byte[] data) throws Exception {
		Key publicK = toPublicKey(publicKey);
		return decode(data, publicK,keySize);
	}

	/**
	 * 解密函数
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public byte[] decode(byte[] data, Key key,int keySize) throws Exception {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);

        int blockSize = keySize / 8;
		// 对数据分段解密
		int inputLen = data.length;
		int offSet = 0;
		byte[] cache;
		int i = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			while (inputLen > offSet) {
				if (inputLen - offSet > blockSize) {
					cache = cipher.doFinal(data, offSet, blockSize);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * blockSize;
			}
			byte[] decryptedData = out.toByteArray();
			return decryptedData;
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(out);
		}

	}

	/**
	 * 公钥加密
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public  byte[] encodeByPublicKey(byte[] data) throws Exception {
		Key publicK = toPublicKey(publicKey);
		return encode(data, publicK,keySize);
	}

	/**
	 * 私钥加密
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] encodeByPrivateKey(byte[] data) throws Exception {
		Key privateK = toPrivateKey(privateKey);
		return encode(data, privateK,keySize);
	}

	/**
	 * 加密函数
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encode(byte[] data, Key key,int keySize) throws Exception {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);

        int blockSize = keySize / 8 - 11;

		// 对数据分段加密
		int inputLen = data.length;
		int offSet = 0;
		byte[] cache;
		int i = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			while (inputLen > offSet) {
				if (inputLen - offSet > blockSize) {
					cache = cipher.doFinal(data, offSet, blockSize);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * blockSize;
			}
			byte[] encryptedData = out.toByteArray();
			return encryptedData;
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 获取私钥
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public static byte[] getPrivateKey(KeyPair keyPair) throws Exception {
		Key key = keyPair.getPrivate();
		return (key.getEncoded());
	}

	/**
	 * 获取公钥
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public static  byte[] getPublicKey(KeyPair keyPair) throws Exception {
		Key key = keyPair.getPublic();
		return (key.getEncoded());
	}
	
	/**
	 * 获取Modulus
	 * @param keyPair
	 * @return
	 */
	public static  byte[] getModulus(KeyPair keyPair){
		RSAPrivateKey key = (RSAPrivateKey)keyPair.getPrivate();
		return (key.getModulus().toByteArray());
	}
	
	/**
	 * 获取Modulus
	 * @param key
	 * @return
	 */
	public static  byte[] getModulus(RSAPrivateKey key){
		return (key.getModulus().toByteArray());
	}
	
	/**
	 * 获取Modulus
	 * @param key
	 * @return
	 */
	public static  byte[] getModulus(RSAPublicKey key){
		return (key.getModulus().toByteArray());
	}
	
	/**
	 * 解码私钥
	 * @param keyBytes
	 * @return
	 * @throws Exception
	 */
	public static RSAPrivateKey toPrivateKey(byte[] keyBytes)throws Exception{
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		RSAPrivateKey key = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
		return key;
	}
	
	/**
	 * 解码公钥
	 * @param keyBytes
	 * @return
	 * @throws Exception
	 */
	public static RSAPublicKey toPublicKey(byte[] keyBytes)throws Exception{
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		RSAPublicKey key = (RSAPublicKey)keyFactory.generatePublic(keySpec);
		return key;
	}


}
