package com.block.model;

import java.time.Instant;
import java.util.List;

import com.block.commons.Hasher;


public class Block {
	private int index;
	private String hash;
	private String previousHash;
	private Instant timestamp;
	private List<Transaction> transactions;
	private int difficulty;
	private int nonce;

	public Block(String previousHash, int index, List<Transaction> data) {
		this.previousHash = previousHash;
		this.index = index + 1;
		this.transactions = data;
		timestamp = Instant.now();
		this.difficulty = 0;
		this.nonce = 0;
		hash = Hasher.getHash(index + previousHash + timestamp + getTransactionHashes() + difficulty + nonce);
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

	public Instant getTimestamp() {
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
}
