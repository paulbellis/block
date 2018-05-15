package com.block.service;

import com.block.model.Block;
import com.block.model.ResultSet;

public class BlockService {

	public static boolean processNewBlock(Block b, Ledgers ledger, String originatingIP) {
		return ledger.processIncomingBlock(b, originatingIP);
	}

	private static Block getBlockByHash(String hash, Ledgers ledger) {
		return ledger.getBlock(hash);
	}

	private static Block getBlockByIndex(Integer i, Ledgers ledger) {
		return ledger.getBlock(i);
	}

	public static Object getBlock(String hash, String index, Ledgers ledger) {
		Integer i = null;
		if (ledger == null) {
			return new ResultSet.ResultSetBuilder().setErrorStatus().setData("Null Pointer").build();
		}
		if (hash == null && index == null) {
			return new ResultSet.ResultSetBuilder().setErrorStatus().setData("Invalid Argument").build();
		}
		if (hash == null) {
			try {
				i = Integer.valueOf(index);
			} catch (NumberFormatException e) {
				return new ResultSet.ResultSetBuilder().setErrorStatus().setData(e.getMessage()).build();
			}
		}
		Block b = null;
		if (hash != null) {
			b = getBlockByHash(hash, ledger);
		} else {
			b = getBlockByIndex(i, ledger);
		}
		if (b == null) {
			return new ResultSet.ResultSetBuilder().setErrorStatus().setData(b).build();
		} else {
			return new ResultSet.ResultSetBuilder().setOkStatus().setData(b).build();
		}
	}
}
