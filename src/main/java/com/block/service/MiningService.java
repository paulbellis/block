package com.block.service;

import com.block.commons.JSON;
import com.block.model.Block;
import com.block.model.DummyStore;

public class MiningService {

	public static String mine(Ledgers ledger, DummyStore db, BroadcastService broadcastService) {
		Block b = ledger.mineBlock();
		if (b != null) {
			//db.update(Transaction.COINBASE_AMOUNT);
			broadcastService.broadcastBlock(b);
		}
		return JSON.toJson(b);
	}
}
