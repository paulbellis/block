package com.block.service;

import com.block.model.Block;
import com.block.model.Ledger1;

public class MiningService {

	public static Block mine(Ledger1 ledger, BroadcastService broadcastService) {
		Block b = ledger.createNewBlockInChain();
		if (b != null) {
			broadcastService.broadcastBlock(b);
		}
		return b;
	}
}
