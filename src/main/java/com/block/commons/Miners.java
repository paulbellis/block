package com.block.commons;

import java.time.Instant;
import java.util.List;

import com.block.model.Block;
import com.block.model.Transaction;

public interface Miners {

	Block findBlock(int nextIndex, String previousHash, Instant nextTimestamp, List<Transaction>  blockData, int difficulty);

}
