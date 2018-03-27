package com.block.manager;

import com.block.service.BlockService;
import com.block.service.Ledgers;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetBlockManager implements Route {

	Ledgers ledger;
	
	public GetBlockManager(Ledgers ledger2) {
		super();
		this.ledger = ledger2;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		String hash = request.params("hash");
		String index = request.queryParams("index");
		return BlockService.getBlock(hash, index, ledger);
	}

}
