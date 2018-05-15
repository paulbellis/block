package com.block.commons;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;

import org.junit.Test;

import com.block.model.Block;
import com.block.model.Transaction;

public class TestBlockchainMiner {

	@Test
	public void testFindBlock() {
		BlockchainMiner bcm = new BlockchainMiner();
		Block b = bcm.findBlock(0, "previousHash", Instant.ofEpochMilli(100), new ArrayList<Transaction>(), 4);
		assertTrue(b!=null);
	}

	@Test
	public void testFindBlockReturnsTrueWhenDifficultyZero() {
		BlockchainMiner bcm = new BlockchainMiner();
		Block b = bcm.findBlock(0, "previousHash", Instant.ofEpochMilli(100), new ArrayList<Transaction>(), 0);
		assertTrue(b!=null);
	}

}
