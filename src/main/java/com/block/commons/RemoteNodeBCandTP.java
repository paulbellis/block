package com.block.commons;

import java.util.List;

import com.block.model.Block;
import com.block.model.Transaction;

public class RemoteNodeBCandTP {
	List<Block> blockChain;
	List<Transaction> transactionPool;
	
	private RemoteNodeBCandTP(List<Block> blockChain, List<Transaction> transactionPool) {
		super();
		this.blockChain = blockChain;
		this.transactionPool = transactionPool;
	}

	public List<Block> getBlockChain() {
		return blockChain;
	}

	public List<Transaction> getTransactionPool() {
		return transactionPool;
	}

	public static RemoteNodeBCandTP valueOf(List<Block> blockChain, List<Transaction> transactionPool) {
		return new RemoteNodeBCandTP(blockChain, transactionPool);
	}
}
