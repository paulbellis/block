package com.block.manager;

import java.util.List;

import com.block.commons.JSON;
import com.block.commons.RemoteNodeBCandTP;
import com.block.service.BroadcastService;
import com.block.service.Ledgers;
import com.google.gson.reflect.TypeToken;

import spark.Request;
import spark.Response;
import spark.Route;

public class PostServersManager implements Route {

	private BroadcastService broadcastService;
	private Ledgers ledger;

	public PostServersManager(BroadcastService broadcastService, Ledgers ledger) {
		super();
		this.broadcastService = broadcastService;
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		String body = request.body();
		List<String> addresses = JSON.fromJsonToList(body , new TypeToken<List<String>>(){}.getType()); 
		if (!addresses.isEmpty()) {
			broadcastService.addAddresses(addresses);
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
		return 0;
	}


}
