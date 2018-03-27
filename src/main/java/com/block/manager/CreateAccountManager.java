package com.block.manager;

import java.math.BigDecimal;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.commons.JSON;
import com.block.model.AccountBalance;
import com.block.model.DummyStore;
import com.block.service.Ledgers;

import spark.Request;
import spark.Response;
import spark.Route;

public class CreateAccountManager implements Route {

	private static Logger log = LogManager.getLogger(CreateAccountManager.class);
	private DummyStore db;
	private Ledgers ledger;
	
	public CreateAccountManager(DummyStore db, Ledgers ledger2) {
		this.db = db;
		this.ledger = ledger2;
	}


	public Object handle(Request request, Response response) throws Exception {
		AccountBalance ab = (AccountBalance) JSON.fromJson(request.body(), AccountBalance.class);
		db.put(ab.getAccountId(),ab);
		ledger.createTransaction(ab.getAccountId(),new BigDecimal(ab.getBalance()));
		return "Ok";
	}

}
