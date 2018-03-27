package com.block.service;

import com.block.commons.JSON;

public class UnspentTransactionService {

	public static Object getUnspentTxMap(Ledgers ledger) {
		return JSON.toJson(ledger.getUnspentTxOutsMap());
	}

}
