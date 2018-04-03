package com.block.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.commons.AccountNotExistException;
import com.block.commons.BlockchainMiner;
import com.block.commons.InsufficientFundsException;
import com.block.commons.JSON;
import com.block.commons.Miners;
import com.block.commons.TxInException;
import com.block.model.Block;
import com.block.model.Transaction;
import com.block.model.TxIn;
import com.block.model.TxOut;
import com.block.model.UnspentTxOut;

public class Ledger1 implements Ledgers {
	private static Logger log = LogManager.getLogger(Ledger1.class);
	private static final int MAX_TRANSACTION_SIZE = 1000;
	public static final Object GET_LAST_BLOCK = "last";

	private List<Block> blockChainLedger;

	private Queue<Transaction> transactionPool;
	private Map<String, Queue<UnspentTxOut>> unspentTxOutsMap;
	private Map<String, BigDecimal> balances;
	private BigDecimal moneyInSystem;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
	private BroadcastService broadcastService;
	private int DIFFICULTY = 0;

	private String hashOfLastProcessedBlock = null;

	public Ledger1(List<Block> blockChainLedger, BroadcastService broadcastService) {
		this.broadcastService = broadcastService;
		log.debug("CREATING LEDGER1");
		if (blockChainLedger == null) {
			this.blockChainLedger = new ArrayList<>();
			Block block = Block.createGenesisBlock();
			addBlockToChain(block);
			setHashOfLastProcessedBlock(block.getHash());
			init();
		} else {
			this.blockChainLedger = blockChainLedger;
		}
	}

	private void init() {
		transactionPool = new LinkedList<>();
		unspentTxOutsMap = new HashMap<>();
		balances = new HashMap<>();
		moneyInSystem = new BigDecimal(0);
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

	private List<TxIn> getAllTxInsFromTransactionPoool() {
		return transactionPool.stream()
				.map((Transaction t) -> t.getTxIns())
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

	private List<UnspentTxOut> getAllUnspentTxFromTransactionPool() {
		return transactionPool.stream()
				.map((Transaction tx) -> getUnspentTxFromTransaction(tx))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private BigDecimal calculateBalanceFromLedger(String accountId) {
		List<UnspentTxOut> uTxOs = getAllUnspentTxFromLedger().stream()
				.filter(u -> u.getAddress()
						.equals(accountId))
				.collect(Collectors.toList());
		List<TxIn> txIns = getAllTxInsFromLedger();
		txIns.addAll(getAllTxInsFromTransactionPoool());
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
		return (balance.isPresent() ? balance.get() : new BigDecimal(0));
	}

	private BigDecimal calculateBalance(Collection<UnspentTxOut> queue) {
		Optional<BigDecimal> o = queue.stream()
				.map(UnspentTxOut::getAmount)
				.reduce(BigDecimal::add);
		return (o.isPresent() ? o.get() : new BigDecimal(0));
	}

	private void createNewUnspentTxInMap(String id) {
		unspentTxOutsMap.putIfAbsent(id, new LinkedList<UnspentTxOut>());
	}

	private void updateBalance(String address, BigDecimal amount) {
		balances.putIfAbsent(address, new BigDecimal(0));
		if (balances.get(address) != null) {
			balances.put(address, balances.get(address)
					.add(amount));
		} else {
			balances.put(address, amount);
		}
	}

	private void setBalance(String address, BigDecimal amount) {
		balances.putIfAbsent(address, new BigDecimal(0));
		balances.put(address, amount);
	}

	private boolean processTransaction(Transaction t) {
		if (t != null) {
			List<UnspentTxOut> allUTxOs = getUnspentTxFromTransaction(t);
			List<UnspentTxOut> spent = null;
			try {
				spent = getSpentTxOutsFromTxIns(t.getTxIns());
			} catch (TxInException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			txRemoveSpentTxsFromUnspentTxOuts(spent);
			Map<String, List<UnspentTxOut>> mapSpent = spent
					.stream()
					.collect(Collectors.groupingBy(UnspentTxOut::getAddress));
			mapSpent.entrySet()
					.forEach(e ->
						{
							BigDecimal delta = calculateBalance(e.getValue());
							delta = delta.multiply(new BigDecimal(-1));
							updateBalance(e.getKey(), delta);
						});
			allUTxOs.removeAll(spent);
			addToUnspentTxOutsMap(allUTxOs);
			Map<String, List<UnspentTxOut>> mapReceived = allUTxOs.stream()
					.collect(Collectors.groupingBy(UnspentTxOut::getAddress));
			mapReceived.entrySet()
					.forEach(e ->
						{
							BigDecimal delta = calculateBalance(e.getValue());
							updateBalance(e.getKey(), delta);
						});
			// for (UnspentTxOut uTxO : getUnspentTxFromTransaction(t)) {
			// createNewUnspentTxInMap(uTxO.getAddress());
			// unspentTxOutsMap.get(uTxO.getAddress()).add(uTxO);
			// BigDecimal balance =
			// calculateBalance(unspentTxOutsMap.get(uTxO.getAddress()));
			// updateBalance(uTxO.getAddress(), balance);
			// updateMoneyInSystem(balance);
			// }
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean processIncomingTransaction(Transaction t) {
		try {
			w.lock();
			if (processTransaction(t)) {
				if (addTransactionToPool(t)) {
					//updateMoneyInSystem(amount);
					return true;
				}
			}
			return false;
		}
		finally {
			w.unlock();
		}
		
	}
	@Override
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
			TxOut txOut = TxOut.valueOf(id, amount, 0);
			txOuts.add(txOut);
			t = Transaction.valueOf(txIns, txOuts);
			if (processTransaction(t)) {
				if (addTransactionToPool(t)) {
					broadcastService.broadcastTransaction(t);
				}
			}
			updateMoneyInSystem(amount);
			// if (t != null) {
			// for (UnspentTxOut uTxO : getUnspentTxFromTransaction(t)) {
			// createNewUnspentTxInMap(id);
			// unspentTxOutsMap.get(uTxO.getAddress()).add(uTxO);
			// BigDecimal balance =
			// calculateBalance(unspentTxOutsMap.get(uTxO.getAddress()));
			// balances.put(uTxO.getAddress(), balance);
			// updateMoneyInSystem(balance);
			// }
			// if (addTransactionToPool(t)) {
			// broadcastService.broadcastTransaction(t);
			// }
			//
			// }

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

	@Override
	public BigDecimal getMoneyInSystem() {
		return moneyInSystem;
	}

	private void blockRemoveSpentTxsFromUnspentTxOuts(Block b) {
		b.getTransactions()
				.forEach(t -> txRemoveSpentTxsFromUnspentTxOuts(t));
	}

	private Queue<UnspentTxOut> getUnspentTxOuts(String address) {
		createNewUnspentTxInMap(address);
		return unspentTxOutsMap.get(address);
	}

	private List<UnspentTxOut> getSpentTxOutsFromTxIns(List<UnspentTxOut> allUTxOs, List<TxIn> txIns) {
 		List<UnspentTxOut> spent = new ArrayList<>();
		for (UnspentTxOut uTxO : allUTxOs) {
			for (TxIn txIn : txIns) {
				if (uTxO.getTxOutId()
						.equals(txIn.getTxOutId()) && uTxO.getTxOutIndex() == txIn.getTxOutIndex()) {
					spent.add(uTxO);
					break;
				}
			}
		}
		return spent;
	}

	private List<UnspentTxOut> getSpentTxOutsFromTxIns(List<TxIn> txIns) throws TxInException {
 		List<UnspentTxOut> spent = new ArrayList<>();
 		
		for (TxIn txIn : txIns) {
			
			Optional<UnspentTxOut> uTxO = unspentTxOutsMap
					.values()
					.stream()
					.flatMap(Queue::stream)
					.filter(u -> u.getTxOutId().equals(txIn.getTxOutId()) && u.getTxOutIndex() == txIn.getTxOutIndex())
					.findFirst();
			
			if (uTxO.isPresent()) {
				spent.add(uTxO.get());
			}
			else {
	 			throw new TxInException();
	 		}
		}
		return spent;
	}

	private void txRemoveSpentTxsFromUnspentTxOuts(Transaction t) {
		List<UnspentTxOut> newUTxOuts = getUnspentTxFromTransaction(t);
		List<UnspentTxOut> spentUTxOs = null;
		try {
			spentUTxOs = getSpentTxOutsFromTxIns(t.getTxIns());
		} catch (TxInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (spentUTxOs != null) {
			for (UnspentTxOut spentUTxOut : spentUTxOs) {
				Queue<UnspentTxOut> existingUTxOuts = getUnspentTxOuts(spentUTxOut.getAddress());
				for (UnspentTxOut eUTxOuts : existingUTxOuts) {
					if (eUTxOuts.equals(spentUTxOut)) {
						existingUTxOuts.remove(eUTxOuts);
					}
				}
			}
		}
	}

	private void txRemoveSpentTxsFromUnspentTxOuts(List<UnspentTxOut> uTxOs) {
		for (UnspentTxOut spentUTxOut : uTxOs) {
			Queue<UnspentTxOut> existingUTxOuts = getUnspentTxOuts(spentUTxOut.getAddress());
			for (UnspentTxOut eUTxOuts : existingUTxOuts) {
				if (eUTxOuts.equals(spentUTxOut)) {
					existingUTxOuts.remove(eUTxOuts);
				}
			}
		}
	}

	private void removeTransactionsFromTransactionPool(List<Transaction> txs) {
		for (Transaction tx : txs) {
			transactionPool.removeIf((Transaction t) -> t.getId()
					.equals(tx.getId()));
		}
	}

	private void writeBlockToDisk(Block b) {
		String filename = "/tmp/" + b.getHash() + ".txt";
		Path path = Paths.get(filename);
		try {
			Files.write(path, JSON.toJson(b)
					.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void calculateBalancesFromBlock(Block b) {
		Map<String, List<UnspentTxOut>> x = getUnspentTxFromBlock(b).stream()
				.collect(Collectors.groupingBy((UnspentTxOut u) -> u.getAddress()));
		x.keySet()
				.forEach(s ->
					{
						BigDecimal balance = calculateBalanceFromLedgerAndTp(s);
						setBalance(s, balance);
						updateMoneyInSystem(balance);
					});
	}

	@Override
	public void addNewBlockToChain(Block b) {
		try {
			w.lock();
			addBlockToChain(b);
			setHashOfLastProcessedBlock(b.getHash());
			b.getTransactions().forEach(t -> {
				if (!transactionInTransactionPool(t)) {
					processTransaction(t);
				}
			});
			removeTransactionsFromTransactionPool(b.getTransactions());
			//blockRemoveSpentTxsFromUnspentTxOuts(b);
			writeBlockToDisk(b);
		} finally {
			w.unlock();
		}
	}

	private boolean transactionInTransactionPool(Transaction tx) {
		return (transactionPool.stream().filter(t -> t.getId().equals(tx.getId())).findFirst().isPresent());
	}

	@Override
	public void addIncomingBlockToChain(Block incomingBlock, String originatingIP) {
		try {
			w.lock();

			Block myLast = getCurrentLastBlock();
			if (myLast != null) {
				if (incomingBlock.getIndex() - myLast.getIndex() > 1) {
					log.error("incoming block has index greater than one greater than current last " + incomingBlock
							+ " " + myLast);
				} else {
					addNewBlockToChain(incomingBlock);
					calculateBalancesFromBlock(incomingBlock);
				}
			} else {
				log.error("cannot get mylast block. got null");
			}
		} finally {
			w.unlock();
		}
	}

	@Override
	public Block getCurrentLastBlock() {
		try {
			r.lock();
			if (blockChainLedger != null && blockChainLedger.size() > 0) {
				return blockChainLedger.get(blockChainLedger.size() - 1);
			}
			return null;
		} finally {
			r.unlock();
		}
	}

	@Override
	public Block mineBlock() {
		try {
			w.lock();
			Block currentLastBlock = getCurrentLastBlock();
			List<Transaction> transList = transactionPool.stream()
					.collect(Collectors.toList());
			Transaction reward = Transaction.createCoinBase("");
			//processTransaction(reward);
			transList.add(reward);
			Miners miner = new BlockchainMiner();
			Block b = miner.findBlock(currentLastBlock.getIndex() + 1, currentLastBlock.getHash(), Instant.now(),
					transList, DIFFICULTY);
			if (b != null) {
				addNewBlockToChain(b);
				return b;
			}
		} finally {
			w.unlock();
		}
		return null;
	}

	private boolean isUTxoInUnspentTransactions(Queue<UnspentTxOut> unspentTxOuts, UnspentTxOut uTxO) {
		return unspentTxOuts.stream()
				.anyMatch(u -> u.getTxOutId() == uTxO.getTxOutId() && u.getTxOutIndex() == uTxO.getTxOutIndex());

	}

	private void calculateAllBalancesFromUnspentTxOutsMap() {
		unspentTxOutsMap.entrySet()
				.forEach(e ->
					{
						updateBalance(e.getKey(), calculateBalance(unspentTxOutsMap.get(e.getKey())));
					});

	}

	private void addToUnspentTxOutsMap(List<UnspentTxOut> uTxOs) {
		for (UnspentTxOut uTxO : uTxOs) {
			createNewUnspentTxInMap(uTxO.getAddress());
			if (!isUTxoInUnspentTransactions(unspentTxOutsMap.get(uTxO.getAddress()), uTxO)) {
				unspentTxOutsMap.get(uTxO.getAddress()).add(uTxO);
			}
		}
	}
	
	@Override
	public boolean addTransactionToPool(Transaction tx) {
		try {
			w.lock();
//			if (tx != null) {
//				addToUnspentTxOutsMap(getUnspentTxFromTransaction(tx));
//			}
//			calculateAllBalancesFromUnspentTxOutsMap();
			return transactionPool.add(tx);
		} finally {
			w.unlock();
		}

	}

	private BigDecimal calculateBalanceFromLedgerAndTp(String accountId) {
		return calculateBalanceFromLedger(accountId).add(calculateBalanceFromTransactionPool(accountId));
	}

	private boolean audit(String from, String to) {
		BigDecimal fromLedgerBalance = calculateBalanceFromLedgerAndTp(from);
		BigDecimal toLedgerBalance = calculateBalanceFromLedgerAndTp(to);
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
		if (fromLedgerBalance.compareTo(fromBalanceFromBalances) != 0) {
			return false;
		}
		if (toLedgerBalance.compareTo(toBalanceFromBalances) != 0) {
			return false;
		}
		return true;
	}

	private void preChecks(String from, String to, BigDecimal amount)
			throws AccountNotExistException, InsufficientFundsException {
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

	@Override
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

			for (UnspentTxOut uTxO: unspentTxOuts) {
				total = total.add(uTxO.getAmount());
				unspentTxOToCoverAmount.add(uTxO);
				TxIn txIn = new TxIn(uTxO.getTxOutId(), uTxO.getTxOutIndex(), from + "," + to + "," + amount);
				spentTxOuts.add(txIn);
				if (total.compareTo(amount) >= 0) {
					break;
				}
			}
			if (total.compareTo(amount) < 0) {
				throw new InsufficientFundsException();
			}
//			while (total.compareTo(amount) < 0) {
//				UnspentTxOut uTxO = unspentTxOuts..poll();
//				if (uTxO == null && total.compareTo(amount) < 0) {
//					throw new InsufficientFundsException();
//				}
//				if (uTxO != null) {
//					total = total.add(uTxO.getAmount());
//					unspentTxOToCoverAmount.add(uTxO);
//					TxIn txIn = new TxIn(uTxO.getTxOutId(), uTxO.getTxOutIndex(), from + "," + to + "," + amount);
//					spentTxOuts.add(txIn);
//				}
//			}

			int index = 0;
			List<TxOut> txOuts = new ArrayList<>();
			if (total.compareTo(amount) > 0) {
				// create change
				TxOut change = TxOut.valueOf(from, total.subtract(amount), index++);
				txOuts.add(change);
			}
			TxOut txOut = TxOut.valueOf(to, amount, index++);
			txOuts.add(txOut);
			tx = Transaction.valueOf(spentTxOuts, txOuts);
			if (processTransaction(tx)) {
				if (addTransactionToPool(tx)) {
					broadcastService.broadcastTransaction(tx);
				}
			}
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

	@Override
	public synchronized BigDecimal getBalance(String accountId) throws AccountNotExistException {
		BigDecimal balancesBalance = balances.get(accountId);
		BigDecimal ledgerBalance = calculateBalanceFromLedger(accountId);
		BigDecimal transactionPoolBalance = calculateBalanceFromTransactionPool(accountId);

		if (balancesBalance.compareTo(ledgerBalance.add(transactionPoolBalance)) != 0) {
			log.error("ledger balance does not equal balances balance " + balancesBalance + " " + ledgerBalance);
		}
		return balancesBalance;
	}

	private BigDecimal calculateBalanceFromTransactionPool(String accountId) {
		List<UnspentTxOut> uTxOs = getAllUnspentTxFromTransactionPool().stream()
				.filter(u -> u.getAddress()
						.equals(accountId))
				.collect(Collectors.toList());
		List<TxIn> txIns = getAllTxInsFromLedger();
		txIns.addAll(getAllTxInsFromTransactionPoool());
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
		return (balance.isPresent() ? balance.get() : new BigDecimal(0));
	}

	@Override
	public String getBlockChainLedger() {
		return JSON.toJson(blockChainLedger);
	}

	@Override
	public String toString() {
		return "Ledger1 [blockChainLedger=" + blockChainLedger + ", transactionPool=" + transactionPool
				+ ", unspentTxOutsMap=" + unspentTxOutsMap + ", balances=" + balances + ", moneyInSystem="
				+ moneyInSystem + "]";
	}

	@Override
	public Queue<Transaction> getTransactionPool() {
		return transactionPool;
	}

	@Override
	public Block getBlock(String hash) {
		try {
			r.lock();
			if (hash == null) {
				return null;
			} else {
				if (hash.equals(GET_LAST_BLOCK)) {
					return getCurrentLastBlock();
				} else {
					Optional<Block> ob = blockChainLedger.stream()
							.filter((Block b) -> b.getHash()
									.equals(hash))
							.findFirst();
					return (ob.isPresent() ? ob.get() : null);
				}
			}
		} finally {
			r.unlock();
		}
	}

	@Override
	public Block getBlock(Integer index) {
		try {
			r.lock();
			if (index == null) {
				return null;
			}
			Optional<Block> ob = blockChainLedger.stream()
					.filter((Block b) -> b.getIndex() == index)
					.findFirst();
			return (ob.isPresent() ? ob.get() : null);
		} finally {
			r.unlock();
		}
	}

	private String getHashOfLastProcessedBlock() {
		return hashOfLastProcessedBlock;
	}

	private void setHashOfLastProcessedBlock(String hashOfLastProcessedBlock) {
		this.hashOfLastProcessedBlock = hashOfLastProcessedBlock;
	}

	@Override
	public void processNewBlockChain(List<Block> bestBlockChain) {
		List<Block> orderedList = bestBlockChain.stream()
				.sorted((Block b1, Block b2) -> b1.getIndex() - b2.getIndex())
				.collect(Collectors.toList());
		int index = 0;
		for (Block b : orderedList) {
			if (!b.getHash()
					.equals(hashOfLastProcessedBlock)) {
				break;
			}
			index++;
		}
		List<Block> sub = orderedList.subList(index, orderedList.size());
		sub.forEach(b -> addIncomingBlockToChain(b, ""));
	}

	@Override
	public Map<String, Queue<UnspentTxOut>> getUnspentTxOutsMap() {
		return unspentTxOutsMap;
	}

	@Override
	public void processNewTransactionPool(List<Transaction> transactionPool) {
		// TODO Auto-generated method stub
		
	}


}
