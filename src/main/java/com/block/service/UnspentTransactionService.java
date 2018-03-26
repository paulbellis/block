package com.block.service;

import com.block.model.Ledger1;

public class UnspentTransactionService {

	public static Object getUnspentTxMap(Ledger1 ledger) {
		return ledger.getUnspentTxOutsMap();
	}

}
