package com.block.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.block.commons.BlockchainMiner;
import com.block.commons.InsufficientFundsException;
import com.block.commons.JSON;
import com.block.commons.Miners;
import com.block.commons.TxInException;
import com.block.crypto.ECDSA;
import com.block.model.Block;
import com.block.model.BlockStats;
import com.block.model.Transaction;
import com.block.model.TxIn;
import com.block.model.TxOut;
import com.block.model.UnspentTxOut;

public class LedgerService implements Ledgers {
    public static final String GET_LAST_BLOCK = "last";
    public static final int GENESIS_BLOCK_INDEX = 0;

    private static Logger log = LogManager.getLogger(LedgerService.class);

    private Map<String, Queue<UnspentTxOut>> unspentTxOutsMap;
    private Map<String, BigDecimal> balances;
    private Queue<Transaction> transactionPool;
    private List<Block> blockChainLedger;
    private String hashOfLastProcessedBlock = null;
    private BroadcastService broadcastService;
    private KeyService keyService;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    private int difficulty = 4;
    private int cummulativeDifficulty;

    public LedgerService(BroadcastService broadcastService, KeyService key) {
        this(null, broadcastService, key);
    }

    public LedgerService(List<Block> blockChainLedger, BroadcastService broadcastService, KeyService key) {
        this.broadcastService = broadcastService;
        this.keyService = key;
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
    public Optional<Block> getCurrentLastBlock() {
        try {
            r.lock();
            if (blockChainLedger != null && !blockChainLedger.isEmpty()) {
                return Optional.of(blockChainLedger.get(blockChainLedger.size() - 1));
            } else {
                return Optional.empty();
            }
        } finally {
            r.unlock();
        }
    }

    public int getCummulativeDifficulty() {
        return cummulativeDifficulty;
    }

    private void setCummulativeDifficulty(int cummulativeDifficulty) {
        this.cummulativeDifficulty = cummulativeDifficulty;
    }

    private void addBlockToChain(Block b) {
        w.lock();
        try {
            blockChainLedger.add(b);
            setCummulativeDifficulty(calculateCumulativeDifficulty());
            writeBlockToDisk(b);
        } finally {
            w.unlock();
        }
    }

    public Optional<Block> getBlockIndex(Integer index) {
        if (index == null) {
            return Optional.empty();
        }
        try {
            r.lock();
            return blockChainLedger.stream()
                    .filter((Block b) -> b.getIndex() == index)
                    .findFirst();
        } finally {
            r.unlock();
        }
    }

    public Optional<Block> getBlockHash(String hash) {
        try {
            r.lock();
            if (hash == null) {
                return Optional.empty();
            } else {
                if (hash.equals(GET_LAST_BLOCK)) {
                    return getCurrentLastBlock();
                } else {
                    return blockChainLedger.stream()
                            .filter((Block b) -> b.getHash()
                                    .equals(hash))
                            .findFirst();
                }
            }
        } finally {
            r.unlock();
        }
    }

    public List<Block> getBlockChainLedger() {
        return blockChainLedger;
    }

    public int calculateCumulativeDifficulty() {
        double cd = 0;
        for (Block b : blockChainLedger) {
            cd = cd + Math.pow(2, b.getDifficulty());
        }
        return (int) cd;
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

    public Transaction createTransaction(String from, String to, BigDecimal amount) throws InsufficientFundsException {
        Transaction tx = null;
        try {
            w.lock();
            Queue<UnspentTxOut> unspentTxOuts = unspentTxOutsMap.get(from);
            List<UnspentTxOut> unspentTxOToCoverAmount = new ArrayList<>();
            List<TxIn> txInsRepresentingSpentTxOuts = new ArrayList<>();
            BigDecimal total = new BigDecimal(0);

            if (unspentTxOuts != null) {
                for (UnspentTxOut uTxO : unspentTxOuts) {
                    total = total.add(uTxO.getAmount());
                    unspentTxOToCoverAmount.add(uTxO);
                    TxIn txIn = TxIn.valueOf(uTxO, from + "," + to + "," + amount);
                    txInsRepresentingSpentTxOuts.add(txIn);
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
            tx = Transaction.valueOf(keyService.getKey(from), txInsRepresentingSpentTxOuts, txOuts);
            if (tx != null && processTransaction(tx)) {
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
        if ( uTxOs != null ) {
            for (UnspentTxOut spentUTxOut : uTxOs) {
                Queue<UnspentTxOut> existingUTxOuts = getUnspentTxOuts(spentUTxOut.getAddress());
                for (UnspentTxOut eUTxOuts : existingUTxOuts) {
                    if (eUTxOuts.equals(spentUTxOut)) {
                        existingUTxOuts.remove(eUTxOuts);
                    }
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
            Optional<Block> ob = getCurrentLastBlock();
            if (ob.isPresent()) {
                Block currentLastBlock = ob.get();
                List<Transaction> transList = transactionPool.stream()
                        .collect(Collectors.toList());
                Transaction reward = Transaction.createCoinBase(keyService.getNodePublicKey());
                transList.add(reward);
                Miners miner = new BlockchainMiner();
                Block b = miner.findBlock(currentLastBlock.getIndex() + 1, currentLastBlock.getHash(), Instant.now(),
                        transList, difficulty);
                if (b != null) {
                    addNewBlockToChain(b);
                    return b;
                }
            }
        } finally {
            w.unlock();
        }
        return null;
    }

    // utils
    public synchronized BigDecimal getBalance(String accountId) {
        BigDecimal balancesBalance = balances.get(accountId);
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
                log.error(e1.getMessage());
            }
            if ( spent != null ) {
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
                if ( allUTxOs != null) {
                    Map<String, List<UnspentTxOut>> mapReceived = allUTxOs.stream()
                            .collect(Collectors.groupingBy(UnspentTxOut::getAddress));
                    if ( mapReceived != null ) {
                        mapReceived.entrySet()
                                .forEach(e ->
                                {
                                    BigDecimal delta = calculateBalance(e.getValue());
                                    updateBalance(e.getKey(), delta);
                                });
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean processIncomingTransaction(Transaction t) {
        try {
            w.lock();
            if (!veryifyTransaction(t)) {
                return false;
            }
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

    private boolean veryifyTransaction(Transaction tx) {
        if (tx == null) {
            throw new NullPointerException("tx is null. Not possible");
        }
        try {
            if (tx.getTxIns()
                    .isEmpty()) {
                // assume its a coinbase transaction and let it through
                return true;
            }
            for (TxIn txIn : tx.getTxIns()) {
                if (!ECDSA.verify(ECDSA.getPubKeyFromAddress(txIn.getLinkedUTxO()
                        .getAddress()), tx.hashTxs(), Hex.decodeHex(
                        txIn.getSignature()
                                .toCharArray()))) {
                    return false;
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | DecoderException e) {
            log.error(e.getMessage());
        }
        return true;
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
                    .forEach(tx ->
                    {
                        if (!transactionInTransactionPool(tx)) {
                            processTransaction(tx);
                        }
                    });
            removeTransactionsFromTransactionPool(b.getTransactions());
        } finally {
            w.unlock();
        }
    }

    private void writeBlockToDisk(Block b) {
        Path p = Paths.get("/tmp/" + b.getHash() + ".txt");
        try {
            Files.write(p, JSON.toJson(b).getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean verifyBlock(Block b) {
        for (Transaction tx : b.getTransactions()) {
            if (!veryifyTransaction(tx)) {
                return false;
            }
        }
        return true;
    }

    public boolean processIncomingBlock(Block incomingBlock) {
        try {
            w.lock();
            Optional<Block> ob = getCurrentLastBlock();
            if (ob.isPresent()) {
                Block myLast = ob.get();
                if (myLast != null) {
                    if (incomingBlock.getIndex() - myLast.getIndex() > 1) {
                        log.error("incoming block has index greater than one greater than current last " + incomingBlock
                                + " " + myLast);
                    } else {
                        if (verifyBlock(incomingBlock)) {
                            addNewBlockToChain(incomingBlock);
                        } else {
                            return false;
                        }
                    }
                } else {
                    log.error("cannot get mylast block. got null");
                }
                return true;
            } else {
                return false;
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
        sub.forEach(b -> processIncomingBlock(b));
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

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public BlockStats getStats() {
        BlockStats bs = BlockStats.create();
        bs.setStat(BlockStats.CURRENT_LAST_HEADER, getCurrentLastBlock().get().getHeader());
        bs.setStat(BlockStats.CUMMULATIVE_DIFFICULTY, getCummulativeDifficulty());
        return bs;
    }
}
