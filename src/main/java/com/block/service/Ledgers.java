package com.block.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;
import com.block.model.Block;
import com.block.model.Transaction;
import com.block.model.UnspentTxOut;

public interface Ledgers {

	boolean processIncomingTransaction(Transaction t);

	Transaction createTransaction(String id, BigDecimal amount);

	BigDecimal getMoneyInSystem();

	void addNewBlockToChain(Block b);

	void addIncomingBlockToChain(Block incomingBlock, String originatingIP);

	Block getCurrentLastBlock();

	Block mineBlock();

	boolean addTransactionToPool(Transaction tx);

	Transaction createTransaction(String from, String to, BigDecimal amount)
			throws InsufficientFundsException, AccountNotExistException;

	BigDecimal getBalance(String accountId) throws AccountNotExistException;

	String getBlockChainLedger();

	String toString();

	Queue<Transaction> getTransactionPool();

	Block getBlock(String hash);

	Block getBlock(Integer index);

	void processNewBlockChain(List<Block> bestBlockChain);

	Map<String, Queue<UnspentTxOut>> getUnspentTxOutsMap();

	void processNewTransactionPool(List<Transaction> transactionPool);

}