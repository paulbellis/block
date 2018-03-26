package com.block.rest;

import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.stop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static spark.Spark.port;

import com.block.commons.JSON;
import com.block.model.Ledger1;
import com.block.service.BalanceService;
import com.block.service.BroadcastService;
import com.block.service.CreateAccountManager;
import com.block.service.DummyStore;
import com.block.service.Dump;
import com.block.service.GetBlockchainManager;
import com.block.service.TransactionManager;
import com.block.service.TransferService;
import com.google.gson.reflect.TypeToken;

import spark.Route;

public class ServerIgnite {

	private DummyStore db = new DummyStore();
	private final String url;
	private final int port;
	private BroadcastService broadcastService;
	private Ledger1 ledger;
	private TransferService transferService = new TransferService(db, ledger);

	public ServerIgnite(String url, int port) {
		this.url = url;
		this.port = port;
		broadcastService = new BroadcastService(url,port);
		ledger = new Ledger1(null,broadcastService);
	}

	public void start() {
		port(port);
		get("/ledger", new GetBlockchainManager(ledger));
		get("/balance/:id", new BalanceService(ledger));
		get("/dump", new Dump(db));
		post("/create", new CreateAccountManager(db,ledger));
		put("/transfer", new TransferManager(transferService));
		post("/transaction",new TransactionManager(ledger));
		post("/servers", (request,response) -> {
			List<String> addresses = JSON.fromJsonToList(request.body(),new TypeToken<List<String>>(){}.getType()); 
			if (!addresses.isEmpty()) {
				broadcastService.addAddresses(addresses);
			}
			return 0;
		});
		get("/servers", (request,response) -> {
			return broadcastService.getAddresses();
		});
		broadcastService.getNetworkNodes();
		broadcastService.broadCastMe();

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
		ServerIgnite server = new ServerIgnite(args[0],Integer.valueOf(args[1]));
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
