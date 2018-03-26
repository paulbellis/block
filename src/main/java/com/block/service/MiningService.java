package com.block.service;

import com.block.model.Block;
import com.block.model.Ledger1;
import com.block.model.Transaction;

public class MiningService {

	public static Block mine(Ledger1 ledger, DummyStore db, BroadcastService broadcastService, String nodeAddress) {
		Block b = ledger.mineBlock(nodeAddress);
		if (b != null) {
			db.update(nodeAddress, Transaction.COINBASE_AMOUNT);
			broadcastService.broadcastBlock(b);
		}
		return b;
	}
}
