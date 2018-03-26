package com.block.service;

import com.block.model.Block;
import com.block.model.Ledger1;

public class BlockService {

	public static void processNewBlock(Block b, Ledger1 ledger, String originatingIP) {
		ledger.addIncomingBlockToChain(b,originatingIP);
	}

}
