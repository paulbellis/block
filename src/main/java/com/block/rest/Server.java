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
import java.util.List;

import com.block.manager.GetBlockManager;
import com.block.manager.GetUnspentTransactionsManager;
import com.block.manager.MiningManager;
import com.block.manager.PostBlockManager;
import com.block.model.Block;
import com.block.model.Ledger1;
import com.block.service.BalanceService;
import com.block.service.BroadcastService;
import com.block.service.CreateAccountManager;
import com.block.service.DummyStore;
import com.block.service.Dump;
import com.block.service.GetBlockchainManager;
import com.block.service.GetServersManager;
import com.block.service.PostServersManager;
import com.block.service.TransactionManager;
import com.block.service.TransferService;

public class Server {

	private DummyStore db = new DummyStore();
	private final String url;
	private final int port;
	private BroadcastService broadcastService;
	private Ledger1 ledger;
	private TransferService transferService;
	private String nodeAddress;

	public Server(String url, int port) {
		this.url = url;
		this.port = port;
		broadcastService = new BroadcastService(url,port);
		ledger = new Ledger1(null,broadcastService);
		transferService = new TransferService(db, ledger);
		this.nodeAddress = "1234";
	}

	public void start() {
		port(port);
		get("/ledger", new GetBlockchainManager(ledger));
		get("/balance/:id", new BalanceService(ledger));
		get("/dump", new Dump(db));
		post("/create", new CreateAccountManager(db,ledger));
		put("/transfer", new TransferManager(transferService));
		post("/transaction",new TransactionManager(ledger));
		post("/servers", new PostServersManager(broadcastService));
		get("/servers", new GetServersManager(broadcastService));
		get("/mine", new MiningManager(ledger, db, broadcastService, nodeAddress));
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
		Block myLast = ledger.getCurrentLastBlock();
		List<Block> bestBlockChain = broadcastService.getLastBlock();
		if (bestBlockChain != null && !bestBlockChain.isEmpty()) {
			ledger.processNewBlockChain(bestBlockChain);
		}
	}
	
	public void stopServer() {
		stop();
	}
	
	public void init(String configFilePath) {
		if (configFilePath!=null) {
			Path path = Paths.get(configFilePath);
			try {
				Files.readAllLines(path).forEach((String node) -> broadcastService.addAddress(node));
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server(args[0],Integer.valueOf(args[1]));
		server.init((args.length>2?args[2]:null));
		server.start();
//		Server server1 = new Server("http://localhost",4567);
//		server1.init(null);
//		server1.start();
//		Server server2 = new Server("http://localhost",4568);
//		server2.init("config2.txt");
//		server2.start();
	}

}
