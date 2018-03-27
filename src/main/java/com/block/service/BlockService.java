package com.block.service;

import com.block.commons.JSON;
import com.block.model.Block;

public class BlockService {

	public static void processNewBlock(Block b, Ledgers ledger, String originatingIP) {
		ledger.addIncomingBlockToChain(b,originatingIP);
	}

	public static Object getBlock(String hash, String index, Ledgers ledger) {
		Block b = null;
		if (hash != null) {
			b = ledger.getBlock(hash);
		}
		else {
			if ( index != null ) {
				try {
					Integer i = Integer.valueOf(index);
					b = ledger.getBlock(i);
				} catch (NumberFormatException  e) {
					return "Invalid number for index request of block";
				}
			}
		}
		return (b == null ? "No such block" : JSON.toJson(b));
	}
}
