package com.block.service;

import java.math.BigDecimal;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.commons.JSON;
import com.block.model.AccountBalance;
import com.block.model.AccountTransfer;
import com.block.model.Ledger1;

import spark.Request;
import spark.Response;
import spark.Route;

public class CreateAccountManager implements Route {

	private static Logger log = LogManager.getLogger(CreateAccountManager.class);
	private DummyStore db;
	private Ledger1 ledger;
	
	public CreateAccountManager(DummyStore db, Ledger1 ledger) {
		this.db = db;
		this.ledger = ledger;
	}


	public Object handle(Request request, Response response) throws Exception {
		AccountBalance ab = (AccountBalance) JSON.fromJson(request.body(), AccountBalance.class);
		db.put(ab.getAccountId(),ab);
		ledger.createTransaction(ab.getAccountId(),new BigDecimal(ab.getBalance()));
		return "Ok";
	}

}
