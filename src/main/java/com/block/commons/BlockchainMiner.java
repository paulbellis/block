package com.block.commons;

import java.time.Instant;
import java.util.List;

import com.block.model.Block;
import com.block.model.Transaction;

public class BlockchainMiner implements Miners {

	private String hexCharToFourBitBinary(char hexChar) {
		String binaryString = Integer.toBinaryString(Integer.parseInt(String.valueOf(hexChar), 16));
		while (binaryString.length() < 4) {
			binaryString = "0" + binaryString;
		}
		return binaryString;
	}

	private boolean hashMatchesDifficulty(String hash, int difficulty) {
		if (difficulty == 0) {
			return true;
		}
		String hashInBinary = hash.chars().mapToObj((i) -> hexCharToFourBitBinary((char) i)).reduce((s1, s2) -> s1 + s2)
				.get();
		String requiredPrefix = "0";
		while (requiredPrefix.length() < difficulty) {
			requiredPrefix = requiredPrefix + "0";
		}
		return hashInBinary.startsWith(requiredPrefix);
	}

	@Override
	public Block findBlock(int nextIndex, String previousHash, Instant nextTimestamp, List<Transaction> blockData,
			int difficulty) {
		int nonce = 0;
		while (true) {
			String hash = Hasher.calculateHash(nextIndex, previousHash, nextTimestamp, blockData, difficulty, nonce);
			if (hashMatchesDifficulty(hash, difficulty)) {
				return Block.createBlock(nextIndex, hash, previousHash, nextTimestamp, blockData, difficulty, nonce);
			}
			nonce++;
		}
	}

}
