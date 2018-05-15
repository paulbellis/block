package com.block.service;

import com.block.model.ResultSet;

public class BlockchainService {

	public static String getBlockChainLedger(Ledgers ledger) {
		return new ResultSet.ResultSetBuilder().setOkStatus().setData(ledger.getBlockChainLedger()).build();
	}
}
