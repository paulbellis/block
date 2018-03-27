package com.block.service;

import java.math.BigDecimal;

import com.block.commons.AccountNotExistException;
import com.block.commons.JSON;

import spark.Request;
import spark.Response;
import spark.Route;

public class BalanceService implements Route {
	
	private Ledgers ledger;

	public BalanceService(Ledgers ledger2) {
		this.ledger = ledger2;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		String accountId = request.params(":id");
		BigDecimal balance = null;
		try {
			balance = ledger.getBalance(accountId);
		}
		catch (AccountNotExistException e) {
			return "Account does not exist";
		}
		catch (Exception e) {
			return e.getMessage();
		}
		return JSON.toJson(balance);
	}

}
