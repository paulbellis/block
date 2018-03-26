package com.block.manager;

import com.block.commons.JSON;
import com.block.model.Ledger1;
import com.block.service.UnspentTransactionService;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetUnspentTransactionsManager implements Route {

	private Ledger1 ledger;

	public GetUnspentTransactionsManager(Ledger1 ledger) {
		super();
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return JSON.toJson(UnspentTransactionService.getUnspentTxMap(ledger));
	}

}
