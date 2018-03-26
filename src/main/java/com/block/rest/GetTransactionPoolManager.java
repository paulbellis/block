package com.block.rest;

import com.block.commons.JSON;
import com.block.model.Ledger1;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetTransactionPoolManager implements Route {

	private Ledger1 ledger;

	public GetTransactionPoolManager(Ledger1 ledger) {
		super();
		this.ledger = ledger;
	}
	
	@Override
	public Object handle(Request request, Response response) throws Exception {
		return JSON.toJson(ledger.getTransactionPool());
	}

}
