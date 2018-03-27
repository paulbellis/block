package com.block.service;

import com.block.commons.JSON;
import com.block.model.Transaction;

public class TransactionService {
	public static Object processIncomingTransaction(Ledgers ledger, Transaction tx) {
		ledger.processIncomingTransaction(tx);
		return JSON.toJson(ledger.getTransactionPool());
	}
}
