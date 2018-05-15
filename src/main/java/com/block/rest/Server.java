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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.block.manager.GetBlockManager;
import com.block.manager.GetBlockchainManager;
import com.block.manager.GetServersManager;
import com.block.manager.GetTransactionPoolManager;
import com.block.manager.GetUnspentTransactionsManager;
import com.block.manager.MiningManager;
import com.block.manager.PostBlockManager;
import com.block.manager.TransactionManager;
import com.block.manager.TransferManager;
import com.block.message.ProcessMessage;
import com.block.model.MessageParameters;
import com.block.service.BalanceService;
import com.block.service.BroadcastService;
import com.block.service.KeyService;
import com.block.service.LedgerService;
import com.block.service.Ledgers;

public class Server {

	private static Logger log = LogManager.getLogger(Server.class);

	private String url;
	private int port;
	private BroadcastService broadcastService;
	private Ledgers ledger;
	private MessageParameters params = new MessageParameters();
	
	public void start() {
		port(port);
		get("/ledger", new GetBlockchainManager(ledger));
		get("/balance/:id", new BalanceService(ledger));
		//post("/create", new CreateAccountManager(db, ledger));
		put("/transfer", new TransferManager(ledger));
		post("/transaction", new TransactionManager(ledger));
		//post("/servers", new PostServersManager(broadcastService, ledger));
		post("/servers", new ProcessMessage(params));
		post("/seed", new ProcessMessage(params));
		get("/servers", new GetServersManager(broadcastService));
		post("/stats", new ProcessMessage(params));
		get("/mine", new MiningManager(ledger, broadcastService));
		post("/block", new PostBlockManager(ledger));
		get("/block/:hash", new GetBlockManager(ledger));
		get("/block", new GetBlockManager(ledger));
		get("/pool", new GetTransactionPoolManager(ledger));
		get("/unspent", new GetUnspentTransactionsManager(ledger));
		startup();
	}

	private void startup() {
		broadcastService.peerPing(ledger);
	}

	public void stopServer() {
		stop();
	}

	public void init(String url, int port, String user, String configFilePath) {
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

		params.setBroadcastService(broadcastService);
		params.setLedgerService(ledger);
		
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
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.init(args[0], Integer.valueOf(args[1]), args[2], args[3]);
		server.start();
	}

}
