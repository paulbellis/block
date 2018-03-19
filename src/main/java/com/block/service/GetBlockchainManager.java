package com.block.service;


import com.block.commons.JSON;
import com.block.model.Ledger1;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetBlockchainManager implements Route {

	private Ledger1 ledger;

	public GetBlockchainManager(Ledger1 ledger) {
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return JSON.toJson(ledger.getBlockChainLedger());
	}

}
