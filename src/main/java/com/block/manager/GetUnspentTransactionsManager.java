package com.block.manager;

import com.block.service.Ledgers;
import com.block.service.UnspentTransactionService;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetUnspentTransactionsManager implements Route {

	private Ledgers ledger;

	public GetUnspentTransactionsManager(Ledgers ledger2) {
		super();
		this.ledger = ledger2;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return UnspentTransactionService.getUnspentTxMap(ledger);
	}

}
