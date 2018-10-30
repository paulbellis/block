package com.block.rest;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.stop;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.block.commons.InsufficientFundsException;
import com.block.commons.JSON;
import com.block.message.BroadcastNodesMessage;
import com.block.message.Message;
import com.block.message.MessageBody;
import com.block.message.MessageHeader;
import com.block.model.AccountTransfer;
import com.block.model.Block;
import com.block.model.ResultSet;
import com.block.model.Transaction;
import com.block.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {

    private static Logger log = LogManager.getLogger(Server.class);

    private String url;
    private int port;
    private BroadcastService broadcastService;
    private Ledgers ledger;

    public void startup() {
        broadcastService.peerPing(ledger);
    }

    public void stopServer() {
        stop();
    }

    public void init(String url, int port, String user, String configFilePath) {
        log.info("Starting " + url + ":" + port + " user " + user + " " + configFilePath);
        this.url = url;
        this.port = port;
        broadcastService = new BroadcastService(url, port);
        KeyService keyService = new KeyService();
        try {
            keyService.init();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        keyService.addNodeKey(user);
        ledger = new LedgerService(broadcastService, keyService);

        if (configFilePath != null) {
            Path path = Paths.get(configFilePath);
            if (path.toFile().exists()) {
                try {
                    Files.readAllLines(path).forEach((String node) -> broadcastService.addAddress(node));
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        startup();
    }

    public Ledgers getLedger() {
        return ledger;
    }

    public Object apiGetLedger() {
        return BlockchainService.getBlockChainLedger(ledger);
    }
    public BigDecimal apiGetBalanceById(String accountId) {
        return ledger.getBalance(accountId);
    }
    public String apiGetMine() {
        return MiningService.mine(ledger, broadcastService);
    }

    public Object apiPutTransfer(AccountTransfer transfer) {
        try {
            return TransferService.transfer(ledger, transfer);
        } catch (InsufficientFundsException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Object apiPostTransaction(Transaction tx) {
        return TransactionService.processIncomingTransaction(ledger, tx);
    }
    public String apiGetServers() {
        return broadcastService.getAddresses();
    }
    public String apiPostBlock(Block b) {
        if (!BlockService.processNewBlock(b, ledger)) {
            return "FAILED TO PROCESS BLOCK";
        }
        else {
            return "SUCCESS";
        }

    }
    public Object apiGetBlock(String hash, String index) {
        return BlockService.getBlock(hash, index, ledger);
    }

    public Object apiGetTransactionPool() {
        return TransactionPoolService.getTransactionPool(ledger);
    }

    public Object apiGetUnspent() {
        return UnspentTransactionService.getUnspentTxMap(ledger);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.init(args[0], Integer.valueOf(args[1]), args[2], args[3]);
    }

    public Object apiProcessMessage(Message msg) {
        if (msg != null) {
            MessageHeader header = msg.getHeader();
            MessageBody body = msg.getBody();
            switch (header.getType()) {
                case BROADCAST_NODES:
                    BroadcastNodesMessage bnm = (BroadcastNodesMessage) JSON.fromJson((String)body.getBody(), BroadcastNodesMessage.class);
                    return P2PService.processBroadcastNodesMessage(broadcastService, bnm);
                case SEED_NODE:
                    BroadcastNodesMessage seed = (BroadcastNodesMessage) JSON.fromJson((String)body.getBody(), BroadcastNodesMessage.class);
                    if (!broadcastService.getAddressList().containsAll(seed.getNodes())) {
                        broadcastService.addAddresses(seed.getNodes());
                        broadcastService.getAndProcessBestBlockChain(ledger);
                        broadcastService.broadCastMe();
                    }
                    return new ResultSet.ResultSetBuilder().setOkStatus().build();
                case SERVER_NODES:
                    return new ResultSet.ResultSetBuilder().setOkStatus().setData(broadcastService.getAddresses()).build();
                case GET_LAST_BLOCK_STATS:
                    return new ResultSet.ResultSetBuilder().setOkStatus().setData(ledger.getStats()).build();
                default:
                    return new ResultSet.ResultSetBuilder().setErrorStatus().setData("Unknown message type").build();
            }
        }
        return null;

    }
}
