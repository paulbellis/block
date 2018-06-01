package com.block.service;

import com.block.commons.InsufficientFundsException;
import com.block.commons.JSON;
import com.block.crypto.DummyKey;
import com.block.crypto.Keys;
import com.block.crypto.ECDSA;
import com.block.model.Block;
import com.block.model.Transaction;
import com.block.model.UnspentTxOut;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LedgerServiceTest {

    private static String GENISIS_BLOCK = "{\"header\":{\"index\":0,\"hash\":\"324f086d2eb360f7479454f337e9f2dedd8d1b0f79339976fa07f6f179ef04ff\",\"previousHash\":\"\",\"timestamp\":767358000,\"difficulty\":0,\"nonce\":0,\"merkelRoot\":\"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"},\"transactions\":[]}";
    private static String A_MINED_BLOCK = "{\"header\":{\"index\":1,\"hash\":\"04048095572471bda2020e9cdda5a0806e8c92fb65c8fe1916f86ee440820d7d\",\"previousHash\":\"324f086d2eb360f7479454f337e9f2dedd8d1b0f79339976fa07f6f179ef04ff\",\"timestamp\":1527411229573,\"difficulty\":4,\"nonce\":9,\"merkelRoot\":\"07935e8dafd0ed9644b5e720f75d7c9ea408beb093130371d4155f749d6e4e19\"},\"transactions\":[{\"id\":\"804ce90eb5f04b5929085d013568e2deb2739c0119bd34ab8ae1b4fa0662e027\",\"txIns\":[],\"txOuts\":[{\"address\":\"3059301306072a8648ce3d020106082a8648ce3d030107034200042954842af7503bb4d9c5b8e0d7582aa623ee65e8dcb4008139a97c5b3f70094b647fb0c95dcba2e84ca97929151f80ac1aaf8d0a0380b60f4f2ddebc6fc276de\",\"amount\":50,\"index\":0}]}]}";
    private static String ADDRESS_WITH_CASH = "3059301306072a8648ce3d020106082a8648ce3d030107034200042954842af7503bb4d9c5b8e0d7582aa623ee65e8dcb4008139a97c5b3f70094b647fb0c95dcba2e84ca97929151f80ac1aaf8d0a0380b60f4f2ddebc6fc276de";
    @Mock
    BroadcastService broadcastService;

    @Mock
    KeyService keyService;

    @Test
    public void getCurrentLastBlock() throws Exception {
        Block b = (Block) JSON.fromJson(GENISIS_BLOCK, Block.class);
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.getCurrentLastBlock()
                .get()
                .getHeader()
                .equals(b.getHeader()));
    }

    @Test
    public void getCummulativeDifficulty() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.getCummulativeDifficulty() == 1);
    }

    @Test
    public void getBlockIndexReturnsNullIfPassedNull() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(!ledger.getBlockIndex(null)
                .isPresent());
    }

    @Test
    public void getBlockIndexReturnsBlockIfPassedValidIndex() throws Exception {
        Block b = (Block) JSON.fromJson(GENISIS_BLOCK, Block.class);
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.getBlockIndex(LedgerService.GENESIS_BLOCK_INDEX)
                .get()
                .getHeader()
                .equals(b.getHeader()));
    }

    @Test
    public void getBlockIndexReturnsNullIfPassedInvalidIndex() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(!ledger.getBlockIndex(1)
                .isPresent());
    }

    @Test
    public void getBlockHashReturnsEmptyOptionalIfNullPassedIn() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(!ledger.getBlockHash(null)
                .isPresent());
    }

    @Test
    public void getBlockHashReturnsLastBlock() throws Exception {
        Block b = (Block) JSON.fromJson(GENISIS_BLOCK, Block.class);
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.getBlockHash(LedgerService.GET_LAST_BLOCK)
                .isPresent());
        assertTrue(ledger.getBlockHash(LedgerService.GET_LAST_BLOCK)
                .get()
                .getHeader()
                .equals(b.getHeader()));
    }

    @Test
    public void getBlockHashReturnsBlockOfHashIfCorrectHashPassedIn() throws Exception {
        Block b = (Block) JSON.fromJson(GENISIS_BLOCK, Block.class);
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.getBlockHash("324f086d2eb360f7479454f337e9f2dedd8d1b0f79339976fa07f6f179ef04ff")
                .isPresent());
        assertTrue(ledger.getBlockHash("324f086d2eb360f7479454f337e9f2dedd8d1b0f79339976fa07f6f179ef04ff")
                .get()
                .getHeader()
                .equals(b.getHeader()));
    }

    @Test
    public void getBlockHashReturnsEmptyOptionalIfIncorrectHashPassedIn() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(!ledger.getBlockHash("x")
                .isPresent());
    }

    @Test
    public void getBlockChainLedger() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.getBlockChainLedger() != null);
    }

    @Test
    public void calculateCumulativeDifficultyWithGenesisBlockOnly() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.calculateCumulativeDifficulty() == 1);
    }

    @Test
    public void calculateCumulativeDifficulty() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        assertTrue(ledger.calculateCumulativeDifficulty() == 1);
    }

    @Test
    public void calculateCumulativeDifficultyWithTwoBlocksInChain() throws Exception {
        Block b = (Block) JSON.fromJson(A_MINED_BLOCK, Block.class);
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        ledger.addNewBlockToChain(b);
        assertTrue(ledger.calculateCumulativeDifficulty() == 17);
    }

    @Test(expected = InsufficientFundsException.class)
    public void createTransactionThrowsInsufficientFundsExceptionAsNoBlocksMinedForFromAddress() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        Map<String, Queue<UnspentTxOut>> uTxOutsMap = ledger.getUnspentTxOutsMap();
        Queue<Transaction> txPool = ledger.getTransactionPool();
        ledger.createTransaction("1", "2", new BigDecimal(99));

    }

    @Test
    public void createTransaction() throws Exception {
        Block b = (Block) JSON.fromJson(A_MINED_BLOCK, Block.class);
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        ledger.addNewBlockToChain(b);
        Map<String, Queue<UnspentTxOut>> uTxOutsMap = ledger.getUnspentTxOutsMap();
        Queue<Transaction> txPool = ledger.getTransactionPool();
        ECDSA key = Mockito.mock(ECDSA.class);
        Mockito.when(keyService.getKey(Mockito.anyString())).thenReturn(key);
        Mockito.when(key.sign(Mockito.anyString())).thenReturn("signed".getBytes());
        ledger.createTransaction(ADDRESS_WITH_CASH, "2", new BigDecimal(9));
        Map<String, Queue<UnspentTxOut>> uTxOutsMapAfter = ledger.getUnspentTxOutsMap();
        Queue<Transaction> txPoolAfter = ledger.getTransactionPool();
        assertTrue(txPoolAfter.size()==1);
    }

    @Test
    public void addTransactionToPool() throws Exception {
        LedgerService ledger = new LedgerService(broadcastService, keyService);
        Queue<Transaction> txPool = ledger.getTransactionPool();
        Keys key = new DummyKey();
        Transaction tx = Transaction.valueOf(key, new ArrayList<>(), new ArrayList<>());
        ledger.addTransactionToPool(tx);
        Queue<Transaction> txPoolAfter = ledger.getTransactionPool();
        assertTrue(txPoolAfter.contains(tx));
    }

    @Test
    public void getTransactionPool() throws Exception {
    }

    @Test
    public void processNewTransactionPool() throws Exception {
    }

    @Test
    public void getUnspentTxOutsMap() throws Exception {
    }

    @Test
    public void mineBlock() throws Exception {
    }

    @Test
    public void getBalance() throws Exception {
    }

    @Test
    public void processIncomingTransaction() throws Exception {
    }

    @Test
    public void addNewBlockToChain() throws Exception {
    }

    @Test
    public void processIncomingBlock() throws Exception {
    }

    @Test
    public void processNewBlockChain() throws Exception {
    }

    @Test
    public void createTransaction1() throws Exception {
    }

    @Test
    public void getMoneyInSystem() throws Exception {
    }

    @Test
    public void getDifficulty() throws Exception {
    }

    @Test
    public void setDifficulty() throws Exception {
    }

    @Test
    public void getStats() throws Exception {
    }

}