package com.block.manager;

import com.block.service.BroadcastService;
import com.block.service.Ledgers;
import com.block.service.MiningService;

import spark.Request;
import spark.Response;
import spark.Route;

public class MiningManager implements Route {

	private Ledgers ledger;
	private BroadcastService broadcastService;

	public MiningManager(Ledgers ledger2, BroadcastService broadcastService) {
		super();
		this.ledger = ledger2;
		this.broadcastService = broadcastService;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return MiningService.mine(ledger, broadcastService);
	}


}
