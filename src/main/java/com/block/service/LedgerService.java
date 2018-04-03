package com.block.service;

import java.math.BigDecimal;
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

public class LedgerService implements Ledgers {
	public static final String GET_LAST_BLOCK = "last";

	private static Logger log = LogManager.getLogger(LedgerService.class);

	private Map<String, Queue<UnspentTxOut>> unspentTxOutsMap;
	private Map<String, BigDecimal> balances;
	private Queue<Transaction> transactionPool;
	private List<Block> blockChainLedger;
	private String hashOfLastProcessedBlock = null;
	private BroadcastService broadcastService;
	private CryptoService key;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private int DIFFICULTY = 0;

	public LedgerService(BroadcastService broadcastService, CryptoService key) {
		this(null, broadcastService, key);
	}

	public LedgerService(List<Block> blockChainLedger, BroadcastService broadcastService, CryptoService key) {
		this.broadcastService = broadcastService;
		this.key = key;
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
	}

	// Blockchain
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

	private void addBlockToChain(Block b) {
		w.lock();
		try {
			blockChainLedger.add(b);
		} finally {
			w.unlock();
		}
	}

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

	public String getBlockChainLedger() {
		return JSON.toJson(blockChainLedger);
	}

	// Transaction
	private List<UnspentTxOut> createUTxOFromTransaction(Transaction tx) {
		List<UnspentTxOut> blockUnspextTxOuts = new ArrayList<>();
		for (TxOut txOut : tx.getTxOuts()) {
			UnspentTxOut uto = new UnspentTxOut(tx.getId(), txOut.getIndex(), txOut.getAddress(), txOut.getAmount());
			blockUnspextTxOuts.add(uto);
		}
		return blockUnspextTxOuts;
	}

	public Transaction createTransaction(String from, String to, BigDecimal amount)
			throws InsufficientFundsException, AccountNotExistException {
		Transaction tx = null;
		try {
			w.lock();
			Queue<UnspentTxOut> unspentTxOuts = unspentTxOutsMap.get(from);
			List<UnspentTxOut> unspentTxOToCoverAmount = new ArrayList<>();
			List<TxIn> spentTxOuts = new ArrayList<>();
			BigDecimal total = new BigDecimal(0);

			if (unspentTxOuts != null) {
				for (UnspentTxOut uTxO : unspentTxOuts) {
					total = total.add(uTxO.getAmount());
					unspentTxOToCoverAmount.add(uTxO);
					TxIn txIn = new TxIn(uTxO.getTxOutId(), uTxO.getTxOutIndex(), from + "," + to + "," + amount);
					spentTxOuts.add(txIn);
					if (total.compareTo(amount) >= 0) {
						break;
					}
				}
			}
			if (total.compareTo(amount) < 0) {
				throw new InsufficientFundsException();
			}
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
		} finally {
			w.unlock();
		}
		return tx;
	}

	// TransactionPool
	private boolean transactionInTransactionPool(Transaction tx) {
		return (transactionPool.stream()
				.filter(t -> t.getId()
						.equals(tx.getId()))
				.findFirst()
				.isPresent());
	}

	private void removeTransactionsFromTransactionPool(List<Transaction> txs) {
		for (Transaction tx : txs) {
			transactionPool.removeIf((Transaction t) -> t.getId()
					.equals(tx.getId()));
		}
	}

	public boolean addTransactionToPool(Transaction tx) {
		try {
			w.lock();
			return transactionPool.add(tx);
		} finally {
			w.unlock();
		}

	}

	public Queue<Transaction> getTransactionPool() {
		return transactionPool;
	}

	@Override
	public void processNewTransactionPool(List<Transaction> transactionPool) {
		for (Transaction tx : transactionPool) {
			processIncomingTransaction(tx);
		}

	}

	// UnspentTxOuts Map
	private List<UnspentTxOut> getSpentTxOutsFromTxIns(List<TxIn> txIns) throws TxInException {
		List<UnspentTxOut> spent = new ArrayList<>();
		for (TxIn txIn : txIns) {
			Optional<UnspentTxOut> uTxO = unspentTxOutsMap.values()
					.stream()
					.flatMap(Queue::stream)
					.filter(u -> u.getTxOutId()
							.equals(txIn.getTxOutId()) && u.getTxOutIndex() == txIn.getTxOutIndex())
					.findFirst();

			if (uTxO.isPresent()) {
				spent.add(uTxO.get());
			} else {
				throw new TxInException();
			}
		}
		return spent;
	}

	private void removeSpentTxsFromUnspentTxOuts(List<UnspentTxOut> uTxOs) {
		for (UnspentTxOut spentUTxOut : uTxOs) {
			Queue<UnspentTxOut> existingUTxOuts = getUnspentTxOuts(spentUTxOut.getAddress());
			for (UnspentTxOut eUTxOuts : existingUTxOuts) {
				if (eUTxOuts.equals(spentUTxOut)) {
					existingUTxOuts.remove(eUTxOuts);
				}
			}
		}
	}

	private Queue<UnspentTxOut> getUnspentTxOuts(String address) {
		createNewUnspentTxInMap(address);
		return unspentTxOutsMap.get(address);
	}

	private void createNewUnspentTxInMap(String id) {
		unspentTxOutsMap.putIfAbsent(id, new LinkedList<UnspentTxOut>());
	}

	public Map<String, Queue<UnspentTxOut>> getUnspentTxOutsMap() {
		return unspentTxOutsMap;
	}

	// Balances
	private void updateBalance(String address, BigDecimal amount) {
		balances.putIfAbsent(address, new BigDecimal(0));
		if (balances.get(address) != null) {
			balances.put(address, balances.get(address)
					.add(amount));
		} else {
			balances.put(address, amount);
		}
	}

	// Block
	public Block mineBlock() {
		try {
			w.lock();
			Block currentLastBlock = getCurrentLastBlock();
			List<Transaction> transList = transactionPool.stream()
					.collect(Collectors.toList());
			Transaction reward = Transaction.createCoinBase(key.getNodePublicKey());
			// processTransaction(reward);
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

	// utils
	public synchronized BigDecimal getBalance(String accountId) throws AccountNotExistException {
		BigDecimal balancesBalance = balances.get(accountId);
		// BigDecimal ledgerBalance = calculateBalanceFromLedger(accountId);
		// BigDecimal transactionPoolBalance =
		// calculateBalanceFromTransactionPool(accountId);
		// if (balancesBalance.compareTo(ledgerBalance.add(transactionPoolBalance)) !=
		// 0) {
		// log.error("ledger balance does not equal balances balance " + balancesBalance
		// + " " + ledgerBalance);
		// }
		return balancesBalance;
	}

	private BigDecimal calculateBalance(Collection<UnspentTxOut> queue) {
		Optional<BigDecimal> o = queue.stream()
				.map(UnspentTxOut::getAmount)
				.reduce(BigDecimal::add);
		return (o.isPresent() ? o.get() : new BigDecimal(0));
	}

	private boolean isUTxoInUnspentTransactions(Queue<UnspentTxOut> unspentTxOuts, UnspentTxOut uTxO) {
		return unspentTxOuts.stream()
				.anyMatch(u -> u.getTxOutId() == uTxO.getTxOutId() && u.getTxOutIndex() == uTxO.getTxOutIndex());

	}

	private void addToUnspentTxOutsMap(List<UnspentTxOut> uTxOs) {
		for (UnspentTxOut uTxO : uTxOs) {
			createNewUnspentTxInMap(uTxO.getAddress());
			if (!isUTxoInUnspentTransactions(unspentTxOutsMap.get(uTxO.getAddress()), uTxO)) {
				unspentTxOutsMap.get(uTxO.getAddress())
						.add(uTxO);
			}
		}
	}

	private boolean processTransaction(Transaction t) {
		if (t != null) {
			List<UnspentTxOut> allUTxOs = createUTxOFromTransaction(t);
			List<UnspentTxOut> spent = null;
			try {
				spent = getSpentTxOutsFromTxIns(t.getTxIns());
			} catch (TxInException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			removeSpentTxsFromUnspentTxOuts(spent);
			Map<String, List<UnspentTxOut>> mapSpent = spent.stream()
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

	public boolean processIncomingTransaction(Transaction t) {
		try {
			w.lock();
			if (processTransaction(t)) {
				if (addTransactionToPool(t)) {
					// updateMoneyInSystem(amount);
					return true;
				}
			}
			return false;
		} finally {
			w.unlock();
		}

	}

	private void setHashOfLastProcessedBlock(String hashOfLastProcessedBlock) {
		this.hashOfLastProcessedBlock = hashOfLastProcessedBlock;
	}

	public void addNewBlockToChain(Block b) {
		try {
			w.lock();
			addBlockToChain(b);
			setHashOfLastProcessedBlock(b.getHash());
			b.getTransactions()
					.forEach(t ->
						{
							if (!transactionInTransactionPool(t)) {
								processTransaction(t);
							}
						});
			removeTransactionsFromTransactionPool(b.getTransactions());
			// writeBlockToDisk(b);
		} finally {
			w.unlock();
		}
	}

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
				}
			} else {
				log.error("cannot get mylast block. got null");
			}
		} finally {
			w.unlock();
		}
	}

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
	public String toString() {
		return "Ledger1 [blockChainLedger=" + blockChainLedger + ", transactionPool=" + transactionPool
				+ ", unspentTxOutsMap=" + unspentTxOutsMap + ", balances=" + balances + ", moneyInSystem=" + "]";
	}

	@Override
	public Transaction createTransaction(String id, BigDecimal amount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getMoneyInSystem() {
		// TODO Auto-generated method stub
		return null;
	}

}
