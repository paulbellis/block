package com.block.service;

import java.math.BigDecimal;

import com.block.model.ResultSet;

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
			return new ResultSet.ResultSetBuilder().setOkStatus().setData(balance).build();
		} catch (Exception e) {
			return new ResultSet.ResultSetBuilder().setErrorStatus().setData(e.getMessage()).build();
		}
	}

}
