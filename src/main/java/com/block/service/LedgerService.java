package com.block.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.model.AccountBalance;
import com.block.model.Block;
import com.block.model.Ledger1;
import com.block.model.Transaction;
import com.block.model.TxIn;
import com.block.model.TxOut;
import com.block.model.UnspentTxOut;

public class LedgerService {
	private static Logger log = LogManager.getLogger(LedgerService.class);
	private static final int MAX_TRANSACTION_SIZE = 0;

	private Ledger1 ledger;
	
	public LedgerService(Ledger1 ledger) {
		this.ledger = ledger;
	}

	private void addTransaction(Transaction t, String toAccountId, BigDecimal amount, String fromAccountId) {

		w.lock();
		try {
			boolean added = transactionPool.add(t);
			if (added == false) {
				log.error("Count not add " + toAccountId + "," + fromAccountId + "," + amount);
			}
			if (transactionPool.size() > MAX_TRANSACTION_SIZE) {
				Block currentLast = blockChainLedger.get(blockChainLedger.size() - 1);
				List<Transaction> transList = transactionPool.stream().collect(Collectors.toList());
				Block b = new Block(currentLast.getHash(), currentLast.getIndex(), transList);
				blockChainLedger.add(b);
				transactionPool.remove(t);
			}
		}
		finally {
			w.unlock();
		}
	}

//	public List<UnspentTxOut> getUnspentTxOutForAccount(String accountId) {
//		List<Transaction> txs = 
//				blockChainLedger
//				.stream()
//				.map((Block b) -> b.getTransactions())
//				.flatMap(List::stream)
//				.collect(Collectors.toList());
//		List<UnspentTxOut> unspentTxOuts = new ArrayList<>();
//
//		List<TxIn> txIns = txs
//				.stream()
//				.map((Transaction t) -> t.getTxIns())
//				.flatMap(List::stream)
//				.collect(Collectors.toList());
//		for (Transaction t : txs) {
//			for (TxOut txOut : t.getTxOuts()) {
//				if (txOut.getAddress().equals(accountId)) {
//					UnspentTxOut uto = new UnspentTxOut(t.getId(), txOut.getIndex(), txOut.getAddress(),
//							txOut.getAmount());
//					unspentTxOuts.add(uto);
//				}
//			}
//		}
//		unspentTxOuts = unspentTxOuts.stream().filter((UnspentTxOut uTxO) -> isNotInTxIns(uTxO, txIns))
//				.collect(Collectors.toList());
//		return unspentTxOuts;
//	}

//	private List<UnspentTxOut> getUnspentTxOutForAccountAndAmount(String accountId, BigDecimal amount) {
//		List<UnspentTxOut> unspentTxOuts = getUnspentTxOutForAccount(accountId);
//		List<UnspentTxOut> finalList = new ArrayList<>();
//		BigDecimal total = new BigDecimal(0);
//		for (UnspentTxOut uTxO : unspentTxOuts) {
//			total = total.add(uTxO.getAmount());
//			finalList.add(uTxO);
//			if (total.compareTo(amount) >= 0) {
//				return finalList;
//			}
//
//		}
//		return null;
//	}

	private boolean isNotInTxIns(UnspentTxOut uTxO, List<TxIn> txIns) {
		boolean isPresent = txIns.stream().anyMatch((TxIn txIn) -> txIn.getTxOutId().equals(uTxO.getTxOutId())
				&& txIn.getTxOutIndex() == uTxO.getTxOutIndex());
		return !isPresent;
	}

	private void doTransfer(String toAccountId, BigDecimal amount, String fromAccountId, String hash) {
		if (fromAccountId.equals("1") || toAccountId.equals("1")) {
			log.debug("|doTRansferS|Transfer from " + fromAccountId + " to " + toAccountId + " amount " + amount
					+ " hash |" + hash);
		}
		List<TxOut> txOuts = new ArrayList<>();
		TxOut txOut = new TxOut(toAccountId, amount, txOuts.size());
		txOuts.add(txOut);
		List<TxIn> txIns = new ArrayList<>();
		List<UnspentTxOut> unspentTxOutsToCoverAmount = getUnspentTxOutForAccountAndAmount(fromAccountId, amount);
		if (unspentTxOutsToCoverAmount != null) {
			BigDecimal total = new BigDecimal(0);
			for (UnspentTxOut uTxO : unspentTxOutsToCoverAmount) {
				TxIn txIn = new TxIn(uTxO.getTxOutId(), uTxO.getTxOutIndex(), "");
				txIns.add(txIn);
				total = total.add(uTxO.getAmount());
			}
			if (total.compareTo(amount) > 0) {
				BigDecimal payback = total.subtract(amount);
				TxOut paybackTxOut = new TxOut(fromAccountId, payback, txOuts.size());
				txOuts.add(paybackTxOut);
			}
			Transaction t = Transaction.valueOf(txIns, txOuts);
			if (fromAccountId.equals("1") || toAccountId.equals("1")) {
				log.debug("|doTRansfer|Transfer from " + fromAccountId + " to " + toAccountId + " amount " + amount
						+ " hash |" + hash);
			}
			t.setDescription(
					"doTRansfer : Transfer from " + fromAccountId + " to " + toAccountId + " amount " + amount + " transferhash " + hash);
			addTransaction(t, toAccountId, amount, fromAccountId);
		}
		else {
			
			log.error(rwl.isWriteLockedByCurrentThread() +" doTRansfer failed due to null unspents : Transfer from " + fromAccountId + " to " + toAccountId + " amount " + amount + " transferhash " + hash);
		}
	}

	public void transfer(String toAccountId, BigDecimal amount, String fromAccountId, String hash) {
		w.lock();
		try {
			doTransfer(toAccountId, amount, fromAccountId, hash);
		} catch (Exception e) {
			log.error("ERROR" + e.getMessage());
		} finally {
			w.unlock();
		}
	}

	public void create(String id, int amount) {
		w.lock();
		try {
			TxOut txOut = new TxOut(
					id,
					new BigDecimal(amount),
					0);
			TxIn txIn = new TxIn("", 0, "");
			List<TxOut> txOuts = new ArrayList<>();
			txOuts.add(txOut);
			List<TxIn> txIns = new ArrayList<>();
			txIns.add(txIn);
			Transaction t = Transaction.valueOf(txIns, txOuts);
			addTransaction(t, "genesis", new BigDecimal(amount), "c2");
		}
		finally {
			w.unlock();
		}
	}
	public List<Block> getBlockChainLedger() {
		return blockChainLedger;
	}
}
