package com.block.commons;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.block.model.Transaction;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

public class Hasher {

	private Hasher() {
	}

	public static String getHash(String str) {
		if (str == null) {
			throw new NullPointerException();
		}
		return String.valueOf(DigestUtils.md5Hex(str));
	}
	
	public static String calculateHash(int index, String previousHash, Instant timestamp, List<Transaction> data,
			int difficulty, int nonce) {
		HashKey ho = new HashKey(index, previousHash, timestamp, data, difficulty, nonce);
		String strToHash = JSON.toJson(ho);
		String sha256hex = Hashing.sha256().hashString(strToHash, StandardCharsets.UTF_8).toString();
		return sha256hex;
	}

	public static String getHash256OfString(String str) {
		if (str == null) {
			throw new NullPointerException();
		}
		return getHash256OfBytes(str.getBytes());
	}
	
	public static String getHash256OfBytes(byte[] str) {
		if (str == null) {
			throw new NullPointerException();
		}
		HashCode x = Hashing.sha256().hashBytes(str);
		return x.toString();
	}
	
	public static byte[] getHash256AsBytes(byte[] str) {
		if (str == null) {
			throw new NullPointerException();
		}
		HashCode x = Hashing.sha256().hashBytes(str);
		return x.asBytes();
	}
	

}
