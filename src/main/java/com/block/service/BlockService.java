package com.block.service;

import java.util.Optional;

import com.block.model.Block;
import com.block.model.ResultSet;

public class BlockService {

	public static boolean processNewBlock(Block b, Ledgers ledger) {
		return ledger.processIncomingBlock(b);
	}

	private static Optional<Block> getBlockByHash(String hash, Ledgers ledger) {
		return ledger.getBlockHash(hash);
	}

	private static Optional<Block> getBlockByIndex(Integer i, Ledgers ledger) {
		return ledger.getBlockIndex(i);
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
		Optional<Block> ob;
		if (hash != null) {
			ob = getBlockByHash(hash, ledger);
		} else {
			ob = getBlockByIndex(i, ledger);
		}
		if (ob.isPresent()) {
			return new ResultSet.ResultSetBuilder().setOkStatus().setData(ob.get()).build();
		} else {
			return new ResultSet.ResultSetBuilder().setErrorStatus().build();
		}
	}
}
