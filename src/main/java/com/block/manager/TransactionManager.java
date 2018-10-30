package com.block.manager;

import com.block.commons.JSON;
import com.block.model.Transaction;
import com.block.service.Ledgers;
import com.block.service.TransactionService;

import spark.Request;
import spark.Response;
import spark.Route;

public class TransactionManager implements Route {
	
	private Ledgers ledger;
	public TransactionManager(Ledgers ledger2) {
		this.ledger = ledger2;
	}
	@Override
	public Object handle(Request request, Response response) throws Exception {
		Transaction tx  = (Transaction) JSON.fromJson(request.body(), Transaction.class);
		return TransactionService.processIncomingTransaction(ledger, tx);
	}

}
