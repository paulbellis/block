package com.block.service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.commons.JSON;
import com.block.model.AccountBalance;
import com.block.model.AccountTransfer;

import spark.Request;
import spark.Response;
import spark.Route;

public class Create implements Route {

	private static Logger log = LogManager.getLogger(Create.class);
	private DummyStore db;
	private Ledger ledger;
	
	public Create(DummyStore db, Ledger ledger) {
		this.db = db;
		this.ledger = ledger;
	}


	public Object handle(Request request, Response response) throws Exception {
		AccountBalance ab = (AccountBalance) JSON.fromJson(request.body(), AccountBalance.class);
		db.put(ab.getAccountId(),ab);
		ledger.create(ab.getAccountId(),ab.getBalance());
		return "Ok";
	}

}
