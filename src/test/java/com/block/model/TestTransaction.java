package com.block.model;

import org.junit.Test;

public class TestTransaction {

	@Test
	public void testCreateCoinBase() {
		System.out.println(Transaction.createCoinBase("123456"));
	}

}
