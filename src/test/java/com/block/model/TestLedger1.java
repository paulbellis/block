package com.block.model;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;

public class TestLedger1 {

	@Test
	public void testCreateTransaction() throws InsufficientFundsException, AccountNotExistException {
		Ledger1 ledger = new Ledger1(null);
		ledger.createTransaction("1", new BigDecimal(1000));
		ledger.createTransaction("2", new BigDecimal(0));
		ledger.createTransaction("1", "2", new BigDecimal(100));
		System.out.println(ledger.getBalance("1"));
		System.out.println(ledger.getBalance("2"));
		System.out.println(ledger.getMoneyInSystem());
	}

}