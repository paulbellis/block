package com.block.manager;

import com.block.model.DummyStore;
import com.block.service.BroadcastService;
import com.block.service.Ledgers;
import com.block.service.MiningService;

import spark.Request;
import spark.Response;
import spark.Route;

public class MiningManager implements Route {

	private Ledgers ledger;
	private BroadcastService broadcastService;
	private String nodeAddress;
	private DummyStore db;

	public MiningManager(Ledgers ledger2, DummyStore db, BroadcastService broadcastService, String nodeAddress) {
		super();
		this.ledger = ledger2;
		this.broadcastService = broadcastService;
		this.nodeAddress = nodeAddress;
		this.db = db;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return MiningService.mine(ledger, db, broadcastService, nodeAddress);
	}


}
