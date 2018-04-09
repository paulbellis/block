package com.block.rest;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.stop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.commons.RemoteNodeBCandTP;
import com.block.manager.CreateAccountManager;
import com.block.manager.GetBlockManager;
import com.block.manager.GetBlockchainManager;
import com.block.manager.GetServersManager;
import com.block.manager.GetTransactionPoolManager;
import com.block.manager.GetUnspentTransactionsManager;
import com.block.manager.MiningManager;
import com.block.manager.PostBlockManager;
import com.block.manager.PostServersManager;
import com.block.manager.TransactionManager;
import com.block.model.DummyStore;
import com.block.service.BalanceService;
import com.block.service.BroadcastService;
import com.block.service.Dump;
import com.block.service.KeyService;
import com.block.service.LedgerService;
import com.block.service.Ledgers;

public class Server {

	private static Logger log = LogManager.getLogger(Server.class);
	
	private DummyStore db = new DummyStore();
	private final String url;
	private final int port;
	private BroadcastService broadcastService;
	private Ledgers ledger;

	public Server(String url, int port, String user) {
		this.url = url;
		this.port = port;
		broadcastService = new BroadcastService(url,port);
		KeyService keyService = new KeyService();
		try {
			keyService.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		keyService.addNodeKey(user);
		ledger = new LedgerService(broadcastService, keyService);
	}

	public void start() {
		port(port);
		get("/ledger", new GetBlockchainManager(ledger));
		get("/balance/:id", new BalanceService(ledger));
		get("/dump", new Dump(db));
		post("/create", new CreateAccountManager(db,ledger));
		put("/transfer", new TransferManager(ledger));
		post("/transaction",new TransactionManager(ledger));
		post("/servers", new PostServersManager(broadcastService));
		get("/servers", new GetServersManager(broadcastService));
		get("/mine", new MiningManager(ledger, db, broadcastService));
		post("/block", new PostBlockManager(ledger));
		get("/block/:hash", new GetBlockManager(ledger));
		get("/block", new GetBlockManager(ledger));
		get("/pool", new GetTransactionPoolManager(ledger));
		get("/unspent", new GetUnspentTransactionsManager(ledger));
		startup();
	}

	private void startup() {
		broadcastService.getNetworkNodes();
		broadcastService.broadCastMe();
		RemoteNodeBCandTP r = broadcastService.getBestBlockchain();
		if (r != null && r.getBlockChain() != null && !r.getBlockChain().isEmpty()) {
			ledger.processNewBlockChain(r.getBlockChain());
		}
		if (r != null && r.getTransactionPool() != null && !r.getTransactionPool().isEmpty()) {
			ledger.processNewTransactionPool(r.getTransactionPool());
		}
	}
	
	public void stopServer() {
		stop();
	}
	
	public void init(String configFilePath) {
		if (configFilePath!=null) {
			Path path = Paths.get(configFilePath);
			if (path.toFile().exists()) {
				try {
					Files.readAllLines(path).forEach((String node) -> broadcastService.addAddress(node));
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server(args[0],Integer.valueOf(args[1]),args[2]);
		server.init(args[3]);
		server.start();
	}

}
