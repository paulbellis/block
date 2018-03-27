package com.block.service;

import com.block.commons.JSON;
import com.block.model.Block;
import com.block.model.DummyStore;
import com.block.model.Transaction;

public class MiningService {

	public static String mine(Ledgers ledger, DummyStore db, BroadcastService broadcastService, String nodeAddress) {
		Block b = ledger.mineBlock(nodeAddress);
		if (b != null) {
			db.update(nodeAddress, Transaction.COINBASE_AMOUNT);
			broadcastService.broadcastBlock(b);
		}
		return JSON.toJson(b);
	}
}
