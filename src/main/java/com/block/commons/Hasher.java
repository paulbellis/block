package com.block.commons;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.block.model.Transaction;
import com.google.common.hash.Hashing;

public class Hasher {
	private Hasher() {
	}

	public static String getHash(String str) {
		return String.valueOf(DigestUtils.md5Hex(str));
	}
	
	public static String calculateHash(int index, String previousHash, Instant timestamp, List<Transaction> data,
			int difficulty, int nonce) {
		HashKey ho = new HashKey(index, previousHash, timestamp, data, difficulty, nonce);
		String strToHash = JSON.toJson(ho);
		String sha256hex = Hashing.sha256().hashString(strToHash, StandardCharsets.UTF_8).toString();
		return sha256hex;
	}


}
