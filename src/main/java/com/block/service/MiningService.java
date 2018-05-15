package com.block.service;

import com.block.commons.JSON;
import com.block.model.Block;

public class MiningService {

	public static String mine(Ledgers ledger, BroadcastService broadcastService) {
		Block b = ledger.mineBlock();
		if (b != null) {
			broadcastService.broadcastBlock(b);
		}
		return JSON.toJson(b);
	}
}
