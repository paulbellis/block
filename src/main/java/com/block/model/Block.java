package com.block.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.block.commons.Hasher;


public class Block {

	private int index;
	private String hash;
	private String previousHash;
	private long timestamp;
	private List<Transaction> transactions;
	private int difficulty;
	private int nonce;

	private Block(int index, String hash, String previousHash, Instant timestamp, List<Transaction> transactions,
			int difficulty, int nonce) {
		super();
		this.index = index;
		this.hash = hash;
		this.previousHash = previousHash;
		this.timestamp = timestamp.toEpochMilli();
		this.transactions = transactions;
		this.difficulty = difficulty;
		this.nonce = nonce;
	}

	private String getTransactionHashes() {
		StringBuilder sb = new StringBuilder();
		if (transactions != null) {
			for (Transaction transaction : transactions) {
				sb.append(transaction.getId());
			}
		}
		return sb.toString();
	}

	public int getIndex() {
		return index;
	}

	public String getHash() {
		return hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public Instant getTimestampInstant() {
		return Instant.ofEpochMilli(timestamp);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public int getNonce() {
		return nonce;
	}

	@Override
	public String toString() {
		return "Block [index=" + index + ", hash=" + hash + ", previousHash=" + previousHash + ", timestamp="
				+ timestamp + ", transactions=" + transactions + ", difficulty=" + difficulty + ", nonce=" + nonce
				+ "]";
	}

	public static Block createBlock(int index, String hash, String previousHash, Instant timeStamp,
			List<Transaction> blockData, int difficulty, int nonce) {
		return new Block(index, hash, previousHash, timeStamp, blockData, difficulty, nonce);
	}

	public static Block createGenesisBlock() {
		int index = 0;
		String previousHash = "";
		Instant timestamp = Instant.ofEpochMilli(767358000);
		List<Transaction> data = new ArrayList<>();
		int difficulty = 0;
		int nonce = 0;
		String hash = Hasher.calculateHash(index, previousHash, timestamp, data, difficulty, nonce);
		return new Block(0, hash, "", Instant.now(), new ArrayList<Transaction>() , 0, 0);
	}



}
