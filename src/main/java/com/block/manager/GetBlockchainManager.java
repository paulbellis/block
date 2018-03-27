package com.block.manager;


import com.block.service.BlockchainService;
import com.block.service.Ledgers;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetBlockchainManager implements Route {

	private Ledgers ledger;

	public GetBlockchainManager(Ledgers ledger2) {
		this.ledger = ledger2;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return BlockchainService.getBlockChainLedger(ledger);
	}

}
