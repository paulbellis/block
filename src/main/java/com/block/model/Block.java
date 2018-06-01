package com.block.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.block.commons.Hasher;


public class Block {

	private BlockHeader header = new BlockHeader();
	private List<Transaction> transactions;

	private Block(int index, String hash, String previousHash, Instant timestamp, List<Transaction> transactions,
			int difficulty, int nonce) {
		super();
		this.header.setIndex(index);
		this.header.setHash(hash);
		this.header.setPreviousHash(previousHash);
		this.header.setTimestamp(timestamp.toEpochMilli());
		this.transactions = transactions;
		this.header.setDifficulty(difficulty);
		this.header.setNonce(nonce);
		setMerkelRoot();
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
		return header.getIndex();
	}

	public String getHash() {
		return header.getHash();
	}

	public String getPreviousHash() {
		return header.getPreviousHash();
	}

	public Instant getTimestampInstant() {
		return Instant.ofEpochMilli(header.getTimestamp());
	}

	public long getTimestamp() {
		return header.getTimestamp();
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public int getDifficulty() {
		return header.getDifficulty();
	}

	public int getNonce() {
		return header.getNonce();
	}

	@Override
	public String toString() {
		return "Block [index=" + header.getIndex() + ", hash=" + header.getHash() + ", previousHash=" + header.getPreviousHash() + ", timestamp="
				+ header.getTimestamp() + ", transactions=" + transactions + ", difficulty=" + header.getDifficulty() + ", nonce=" + header.getNonce()
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
		return new Block(0, hash, "", timestamp, new ArrayList<Transaction>() , 0, 0);
	}

	public String getMerkelRoot() {
		return header.getMerkelRoot();
	}

	public static String calculateMerkelRoot(Block b) {
		String mr = "";
		for (Transaction tx : b.getTransactions()) {
			mr = mr + Hasher.getHash256OfString(tx.getId());
		}
		mr = Hasher.getHash256OfString(mr);
		return mr;
	}
	
	private void setMerkelRoot() {
		this.header.setMerkelRoot(Block.calculateMerkelRoot(this));
	}

	public BlockHeader getHeader() {
		return header;
	}

	public void setHeader(BlockHeader header) {
		this.header = header;
	}



}
