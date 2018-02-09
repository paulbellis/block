package com.block.service;


import com.block.commons.JSON;

import spark.Request;
import spark.Response;
import spark.Route;

public class DumpLedger implements Route {

	private Ledger ledger;

	public DumpLedger(Ledger ledger) {
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return JSON.toJson(ledger.getBlockChainLedger());
	}

}
