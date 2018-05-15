package com.block.model;

public class BlockHeader {
	private int index;
	private String hash;
	private String previousHash;
	private long timestamp;
	private int difficulty;
	private int nonce;
	private String merkelRoot;

	public BlockHeader() {
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public String getMerkelRoot() {
		return merkelRoot;
	}

	public void setMerkelRoot(String merkelRoot) {
		this.merkelRoot = merkelRoot;
	}
}