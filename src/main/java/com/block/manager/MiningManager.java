package com.block.manager;

import com.block.commons.JSON;
import com.block.model.Ledger1;
import com.block.service.BroadcastService;
import com.block.service.MiningService;

import spark.Request;
import spark.Response;
import spark.Route;

public class MiningManager implements Route {

	private Ledger1 ledger;
	private BroadcastService broadcastService;

	public MiningManager(Ledger1 ledger, BroadcastService broadcastService) {
		super();
		this.ledger = ledger;
		this.broadcastService = broadcastService;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return JSON.toJson(MiningService.mine(ledger, broadcastService));
	}


}
