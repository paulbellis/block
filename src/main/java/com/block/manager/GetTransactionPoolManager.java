package com.block.manager;

import com.block.service.Ledgers;
import com.block.service.TransactionPoolService;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetTransactionPoolManager implements Route {

	private Ledgers ledger;

	public GetTransactionPoolManager(Ledgers ledger2) {
		super();
		this.ledger = ledger2;
	}
	
	@Override
	public Object handle(Request request, Response response) throws Exception {
		return TransactionPoolService.getTransactionPool(ledger);
	}

}
