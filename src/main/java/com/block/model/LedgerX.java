package com.block.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

public class LedgerX {
	private static Logger log = LogManager.getLogger(LedgerX.class);
	private static final int MAX_TRANSACTION_SIZE = 0;

	private final List<Block> blockChainLedger;
	private final Queue<Transaction> transactionPool = new LinkedList<>();
	private final Map<String, Queue<UnspentTxOut>> unspentTxOutsMap = new HashMap<>();
	private final Map<String, BigDecimal> balances = new HashMap<>();
	private BigDecimal moneyInSystem = new BigDecimal(0);
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public LedgerX(List<Block> blockChainLedger) {
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
		return b.getTransactions()
				.stream()
				.map(Transaction::getTxIns)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private List<TxIn> getAllTxInsFromLedger() {
		return blockChainLedger.stream()
				.map((Block b) -> getTxInsFromBlock(b))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private List<UnspentTxOut> getUnspentTxFromTransaction(Transaction tx) {
		List<UnspentTxOut> blockUnspextTxOuts = new ArrayList<>();
		for (TxOut txOut : tx.getTxOuts()) {
			UnspentTxOut uto = new UnspentTxOut(tx.getId(), txOut.getIndex(), txOut.getAddress(), txOut.getAmount());
			blockUnspextTxOuts.add(uto);
		}
		return blockUnspextTxOuts;
	}

	private List<UnspentTxOut> getUnspentTxFromBlock(Block block) {
		return block.getTransactions()
				.stream()
				.map((Transaction t) -> getUnspentTxFromTransaction(t))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private List<UnspentTxOut> getAllUnspentTxFromLedger() {
		return blockChainLedger.stream()
				.map((Block b) -> getUnspentTxFromBlock(b))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private BigDecimal calculateBalanceFromLedger(String accountId) throws AccountNotExistException {
		List<UnspentTxOut> uTxOs = getAllUnspentTxFromLedger().stream()
				.filter(u -> u.getAddress()
						.equals(accountId))
				.collect(Collectors.toList());
		if (uTxOs.isEmpty()) {
			throw new AccountNotExistException(accountId);
		}
		List<TxIn> txIns = getAllTxInsFromLedger();
		List<UnspentTxOut> spentTxOuts = new ArrayList<>();
		for (TxIn txIn : txIns) {
			Optional<UnspentTxOut> o = uTxOs.stream()
					.filter(u -> txIn.getTxOutId() == u.getTxOutId() && txIn.getTxOutIndex() == u.getTxOutIndex())
					.findFirst();
			if (o.isPresent()) {
				spentTxOuts.add(o.get());
			}
		}
		uTxOs.removeAll(spentTxOuts);
		Optional<BigDecimal> balance = uTxOs.stream()
				.map(u -> u.getAmount())
				.reduce((bd1, bd2) -> bd1.add(bd2));
		return (balance.isPresent()?balance.get():new BigDecimal(0));
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
					unspentTxOutsMap.get(uTxO.getAddress())
							.add(uTxO);
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
		try {
			w.lock();
			transactionPool.add(tx);
			if (transactionPool.size() >= MAX_TRANSACTION_SIZE) {
				Block currentLast = blockChainLedger.get(blockChainLedger.size() - 1);
				List<Transaction> transList = transactionPool.stream()
						.collect(Collectors.toList());
				Block b = new Block(currentLast.getHash(), currentLast.getIndex(), transList);
				blockChainLedger.add(b);
				transactionPool.remove(tx);
				String filename = "/tmp/"+ b.getHash()+".txt";
				Path path = Paths.get(filename);
				try {
					Files.write(path, JSON.toJson(b).getBytes(), StandardOpenOption.CREATE);
				} catch (IOException e) {
					log.error(e.getMessage());
				}
//				try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));) {
//					bw.write(JSON.toJson(b));
//				} catch (IOException e) {
//					log.error(e.getMessage());
//				}
//				try (OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(new File(filename)));) {
//					os.write(JSON.toJson(b));
//				} catch (IOException  e) {
//					log.error(e.getMessage());
//				}
			}
		} finally {
			w.unlock();
		}

	}

	private boolean audit(String from, String to) {
		try {
			BigDecimal fromLedgerBalance = calculateBalanceFromLedger(from);
			BigDecimal toLedgerBalance = calculateBalanceFromLedger(to);
			BigDecimal fromBalanceFromUTxOs = calculateBalance(unspentTxOutsMap.get(from));
			BigDecimal toBalanceFromUTxOs = calculateBalance(unspentTxOutsMap.get(to));
			BigDecimal fromBalanceFromBalances = balances.get(from);
			BigDecimal toBalanceFromBalances = balances.get(to);
			if (fromBalanceFromUTxOs == null || toBalanceFromUTxOs == null || fromBalanceFromBalances == null
					|| toBalanceFromBalances == null) {
				return false;
			}
			if (fromBalanceFromUTxOs.compareTo(fromBalanceFromBalances) != 0) {
				return false;
			}
			if (toBalanceFromUTxOs.compareTo(toBalanceFromBalances) != 0) {
				return false;
			}
			if (fromLedgerBalance.compareTo(fromBalanceFromBalances)!=0) {
				return false;
			}
			if (toLedgerBalance.compareTo(toBalanceFromBalances)!=0) {
				return false;
			}
		} catch (AccountNotExistException e) {
			log.error(e.getMessage());
		}
		return true;
	}

	private void preChecks(String from, String to, BigDecimal amount) throws AccountNotExistException, InsufficientFundsException {
		if (!accountExists(from)) {
			throw new AccountNotExistException(from);
		}
		if (!accountExists(to)) {
			throw new AccountNotExistException(to);
		}
		if (amount.compareTo(getBalance(from)) > 0) {
			throw new InsufficientFundsException();
		}
		// audit
		if (!audit(from, to)) {
			log.error("Balance problem");
			System.exit(1);
		}
		if (from == null || to == null || amount == null || amount.compareTo(new BigDecimal(0)) <= 0) {
			throw new IllegalArgumentException();
		}
	}
	
	public Transaction createTransaction(String from, String to, BigDecimal amount)
			throws InsufficientFundsException, AccountNotExistException {
		Transaction tx = null;
		try {
			w.lock();
			preChecks(from, to, amount);
			BigDecimal originalMoneyInSystem = getMoneyInSystem();
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
				}
				BigDecimal newFromBalance = calculateBalance(unspentTxOutsMap.get(from));
				balances.put(from, newFromBalance);
				BigDecimal newToBalance = calculateBalance(unspentTxOutsMap.get(to));
				balances.put(to, newToBalance);
			}
			addTransactionToPool(tx);
			postChecks(originalMoneyInSystem);
		} finally {
			w.unlock();
		}
		return tx;
	}

	private void postChecks(BigDecimal compareAmount) {
		if (getMoneyInSystem().compareTo(compareAmount) != 0) {
			log.error("System has been robbed");
			System.exit(1);
		}
	}

	private boolean accountExists(String id) {
		return (unspentTxOutsMap.get(id) != null && balances.get(id) != null);
	}

	public synchronized BigDecimal getBalance(String accountId) throws AccountNotExistException {
		BigDecimal balancesBalance = balances.get(accountId);
		BigDecimal ledgerBalance = calculateBalanceFromLedger(accountId);
		if (balancesBalance.compareTo(ledgerBalance)!=0) {
			log.error("ledger balance does not equal balances balance " + balancesBalance + " " + ledgerBalance);
		}
		return balancesBalance;
	}

	public String getBlockChainLedger() {
		return JSON.toJson(blockChainLedger);
	}

	@Override
	public String toString() {
		return "Ledger1 [blockChainLedger=" + blockChainLedger + ", transactionPool=" + transactionPool
				+ ", unspentTxOutsMap=" + unspentTxOutsMap + ", balances=" + balances + ", moneyInSystem="
				+ moneyInSystem + "]";
	}
}
