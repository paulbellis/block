package com.block.service;

import com.block.commons.JSON;
import com.block.model.Ledger1;
import com.block.model.Transaction;

import spark.Request;
import spark.Response;
import spark.Route;

public class TransactionManager implements Route {
	
	private Ledger1 ledger;
	public TransactionManager(Ledger1 ledger) {
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		try {
			Transaction tx  = (Transaction) JSON.fromJson(request.body(), Transaction.class);
			ledger.addTransactionToPool(tx);
		}
		catch (Exception e) {
			
		}
		return JSON.toJson(ledger.getTransactionPool());
	}

}
