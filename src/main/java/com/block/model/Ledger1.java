package com.block.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;
import com.block.commons.JSON;
import com.block.model.AccountBalance;
import com.block.model.Block;
import com.block.model.Transaction;
import com.block.model.TxIn;
import com.block.model.TxOut;
import com.block.model.UnspentTxOut;

public class Ledger1 {
	private static Logger log = LogManager.getLogger(Ledger1.class);
	private static final int MAX_TRANSACTION_SIZE = 0;

	private final List<Block> blockChainLedger;
	private final Queue<Transaction> transactionPool = new LinkedList<>();
	private final Map<String, Queue<UnspentTxOut>> unspentTxOutsMap = new HashMap<>();
	private final Map<String, BigDecimal> balances = new HashMap<>();
	private BigDecimal moneyInSystem = new BigDecimal(0);
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public Ledger1(List<Block> blockChainLedger) {
		log.debug("CREATING LEDGER");
		if (blockChainLedger == null) {
			this.blockChainLedger = new ArrayList<>();
			List<Transaction> list = new ArrayList<>();
			Block block = new Block("", 0, list);
			addBlockToChain(block);
		} else {
			this.blockChainLedger = blockChainLedger;
		}
	}

	private Queue<UnspentTxOut> getUnspentTxOutForAccount(String accountId) {
		r.lock();
		try {
			return unspentTxOutsMap.get(accountId);
		} finally {
			r.unlock();
		}
	}

	private void addBlockToChain(Block b) {
		w.lock();
		try {
			blockChainLedger.add(b);
		} finally {
			w.unlock();
		}
	}

	private List<TxIn> getTxInsFromBlock(Block b) {
		List<TxIn> txIns = b.getTransactions()
				.stream()
				.map((Transaction t) -> t.getTxIns())
				.flatMap(List::stream)
				.collect(Collectors.toList());
		return txIns;
	}

	private List<TxOut> getTxOutFromBlock(Block b) {
		List<TxOut> txOuts = b.getTransactions()
				.stream()
				.map((Transaction t) -> t.getTxOuts())
				.flatMap(List::stream)
				.collect(Collectors.toList());
		return txOuts;
	}

	private List<UnspentTxOut> getUnspentTxFromBlock(Block b) {
		List<UnspentTxOut> blockUnspextTxOuts = new ArrayList<>();
		for (Transaction tx : b.getTransactions()) {
			for (TxOut txOut : tx.getTxOuts()) {
				UnspentTxOut uto = new UnspentTxOut(tx.getId(), txOut.getIndex(), txOut.getAddress(),
						txOut.getAmount());
				blockUnspextTxOuts.add(uto);
			}
		}
		return blockUnspextTxOuts;
	}

	private List<UnspentTxOut> getUnspentTxFromTransaction(Transaction tx) {
		List<UnspentTxOut> blockUnspextTxOuts = new ArrayList<>();
		for (TxOut txOut : tx.getTxOuts()) {
			UnspentTxOut uto = new UnspentTxOut(tx.getId(), txOut.getIndex(), txOut.getAddress(), txOut.getAmount());
			blockUnspextTxOuts.add(uto);
		}
		return blockUnspextTxOuts;
	}

	private BigDecimal calculateBalance(Queue<UnspentTxOut> queue) {
		Optional<BigDecimal> o = queue.stream()
				.map(UnspentTxOut::getAmount)
				.reduce(BigDecimal::add);
		return (o.isPresent() ? o.get() : new BigDecimal(0));
	}

	public Transaction createTransaction(String id, BigDecimal amount) {
		Transaction t = null;
		try {
			w.lock();
			BigDecimal originalMoneyInSystem = getMoneyInSystem();
			if (id == null || amount == null || amount.compareTo(new BigDecimal(0)) < 0) {
				throw new IllegalArgumentException();
			}
			List<TxIn> txIns = new ArrayList<>();
			List<TxOut> txOuts = new ArrayList<>();
			TxOut txOut = new TxOut(id, amount, 0);
			txOuts.add(txOut);
			t = Transaction.valueOf(txIns, txOuts);
			if (t != null) {
				for (UnspentTxOut uTxO : getUnspentTxFromTransaction(t)) {
					unspentTxOutsMap.putIfAbsent(id, new LinkedList<UnspentTxOut>());
					unspentTxOutsMap.get(uTxO.getAddress()).add(uTxO);
					BigDecimal balance = calculateBalance(unspentTxOutsMap.get(uTxO.getAddress()));
					balances.put(uTxO.getAddress(), balance);
					updateMoneyInSystem(balance);
				}
				addTransactionToPool(t);
			}

			if (getMoneyInSystem().compareTo(originalMoneyInSystem.add(amount)) != 0) {
				log.error("Error adding money to system");
				System.exit(1);
			}
		} finally {
			w.unlock();
		}
		return t;

	}

	private void updateMoneyInSystem(BigDecimal amount) {
		moneyInSystem = moneyInSystem.add(amount);
	}

	public BigDecimal getMoneyInSystem() {
		return moneyInSystem;
	}

	private void addTransactionToPool(Transaction tx) {
		transactionPool.add(tx);
		if (transactionPool.size() > MAX_TRANSACTION_SIZE) {
			Block currentLast = blockChainLedger.get(blockChainLedger.size() - 1);
			List<Transaction> transList = transactionPool.stream().collect(Collectors.toList());
			Block b = new Block(currentLast.getHash(), currentLast.getIndex(), transList);
			blockChainLedger.add(b);
			transactionPool.remove(tx);
		}

	}

	private boolean audit(String from, String to) {
		BigDecimal fromBalanceFromUTxOs = calculateBalance(unspentTxOutsMap.get(from));
		BigDecimal toBalanceFromUTxOs = calculateBalance(unspentTxOutsMap.get(to));
		BigDecimal fromBalanceFromBalances = balances.get(from);
		BigDecimal toBalanceFromBalances = balances.get(to);
		if (fromBalanceFromUTxOs==null||toBalanceFromUTxOs==null||fromBalanceFromBalances==null||toBalanceFromBalances==null) {
			return false;
		}
		if (fromBalanceFromUTxOs.compareTo(fromBalanceFromBalances)!=0) {
			return false;
		}
		if (toBalanceFromUTxOs.compareTo(toBalanceFromBalances)!=0) {
			return false;
		}
		return true;
	}
	
	public Transaction createTransaction(String from, String to, BigDecimal amount) throws InsufficientFundsException, AccountNotExistException {
		Transaction tx = null;
		try {
			w.lock();
			if (!accountExists(from)) {
				throw new AccountNotExistException();
			}
			if (!accountExists(to)) {
				throw new AccountNotExistException();
			}
			if (amount.compareTo(getBalance(from)) > 0 ) {
				throw new InsufficientFundsException();
			}
			//audit
			if (!audit(from,to)) {
				log.error("Balance problem");
				System.exit(1);
			}
			BigDecimal originalMoneyInSystem = getMoneyInSystem();
			BigDecimal fromBefore = getBalance(from);
			BigDecimal toBefore = getBalance(to);
			
			if (from == null || to == null || amount == null || amount.compareTo(new BigDecimal(0)) <= 0) {
				throw new IllegalArgumentException();
			}
			Queue<UnspentTxOut> unspentTxOuts = unspentTxOutsMap.get(from);
			List<UnspentTxOut> unspentTxOToCoverAmount = new ArrayList<>();
			List<TxIn> spentTxOuts = new ArrayList<>();
			BigDecimal total = new BigDecimal(0);
			while (total.compareTo(amount) < 0) {
				UnspentTxOut uTxO = unspentTxOuts.poll();
				if (uTxO == null && total.compareTo(amount) < 0) {
					throw new InsufficientFundsException();
				}
				if (uTxO != null) {
					total = total.add(uTxO.getAmount());
					unspentTxOToCoverAmount.add(uTxO);
					TxIn txIn = new TxIn(uTxO.getTxOutId(), uTxO.getTxOutIndex(), from + "," + to + "," + amount);
					spentTxOuts.add(txIn);
				}
			}
			int index = 0;
			List<TxOut> txOuts = new ArrayList<>();
			if (total.compareTo(amount) > 0) {
				// create change
				TxOut change = new TxOut(from, total.subtract(amount), index++);
				txOuts.add(change);
			}
			TxOut txOut = new TxOut(to, amount, index++);
			txOuts.add(txOut);
			tx = Transaction.valueOf(spentTxOuts, txOuts);
			if (tx != null) {
				for (UnspentTxOut uTxO : getUnspentTxFromTransaction(tx)) {
					unspentTxOutsMap.get(uTxO.getAddress())
							.add(uTxO);
					BigDecimal balance = calculateBalance(unspentTxOutsMap.get(uTxO.getAddress()));
					balances.put(uTxO.getAddress(), balance);
				}
			}
			addTransactionToPool(tx);
			if (getMoneyInSystem().compareTo(originalMoneyInSystem) != 0) {
				log.error("System has been robbed");
				System.exit(1);
			}
			if (getBalance(from).compareTo(fromBefore.subtract(amount))!=0) {
				Queue<UnspentTxOut> q = unspentTxOutsMap.get(from);
				q.forEach((UnspentTxOut uTxo)-> {
					System.out.println("audit,"+uTxo);
				});
				BigDecimal u = calculateBalance(q);
				BigDecimal t = getBalance(from);
				log.error("Audit error" + u + t);
				blockChainLedger.stream()
				.map((Block b) -> b.getTransactions())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.map((Transaction tx1) -> tx1.getTxOuts())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.filter((TxOut txOut1) -> txOut1.getAddress().equals(from))
				.forEach((TxOut txOut1) -> {System.out.println(txOut1);} );
				blockChainLedger.stream()
				.map((Block b) -> b.getTransactions())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.map((Transaction tx1) -> tx1.getTxIns())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.filter((TxIn txin1) -> txin1.getSignature().contains(from))
				.forEach((TxIn txin1) -> {System.out.println(txin1);} );
				//System.exit(1);
			}
			if (getBalance(to).compareTo(toBefore.add(amount))!=0) {
				Queue<UnspentTxOut> q = unspentTxOutsMap.get(to);
				q.forEach((UnspentTxOut uTxo)-> {
					System.out.println("audit,"+uTxo);
				});
				BigDecimal u = calculateBalance(q);
				BigDecimal t = getBalance(to);
				log.error("Audit error" + u + t);
				blockChainLedger.stream()
				.map((Block b) -> b.getTransactions())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.map((Transaction tx1) -> tx1.getTxOuts())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.filter((TxOut txOut1) -> txOut1.getAddress().equals(to))
				.forEach((TxOut txOut1) -> {System.out.println(txOut1);} );
				blockChainLedger.stream()
				.map((Block b) -> b.getTransactions())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.map((Transaction tx1) -> tx1.getTxIns())
				.flatMap(List::stream)
				.collect(Collectors.toList())
				.stream()
				.filter((TxIn txin1) -> txin1.getSignature().contains(to))
				.forEach((TxIn txin1) -> {System.out.println(txin1);} );
				//System.exit(1);
			}
		} finally {
			w.unlock();
		}
		return tx;
	}

	private boolean accountExists(String id) {
		return (unspentTxOutsMap.get(id)!=null && balances.get(id) != null);
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
				List<Transaction> transList = transactionPool.stream()
						.collect(Collectors.toList());
				Block b = new Block(currentLast.getHash(), currentLast.getIndex(), transList);
				addBlockToChain(b);
				transactionPool.remove(t);
			}
		} finally {
			w.unlock();
		}
	}
	
	public BigDecimal getBalance(String accountId) {		
		return balances.get(accountId);
	}

	public String getBlockChainLedger() {
		return JSON.toJson(blockChainLedger);
	}
}
