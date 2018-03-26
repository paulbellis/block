package com.block.commons;

import java.time.Instant;
import java.util.List;

import com.block.model.Transaction;

public class HashKey {
	int index;
	String previousHash;
	Instant timestamp;
	List<Transaction> data;
	int difficulty;
	int nonce;

	public HashKey(int index, String previousHash, Instant timestamp, List<Transaction> data, int difficulty,
			int nonce) {
		super();
		this.index = index;
		this.previousHash = previousHash;
		this.timestamp = timestamp;
		this.data = data;
		this.difficulty = difficulty;
		this.nonce = nonce;
	}

}
