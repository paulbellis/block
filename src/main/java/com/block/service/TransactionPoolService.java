package com.block.service;

import com.block.commons.JSON;

public class TransactionPoolService {
	public static Object getTransactionPool(Ledgers ledger) {
		return JSON.toJson(ledger.getTransactionPool());
	}
}
