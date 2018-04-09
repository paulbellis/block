package com.block.model;

import java.math.BigDecimal;

import org.junit.Test;

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;
import com.block.service.BroadcastService;
import com.block.service.KeyService;
import com.block.service.LedgerService;

public class TestLedgerService {

	@Test
	public void testCreateTransaction() throws InsufficientFundsException, AccountNotExistException {
		LedgerService ledger = new LedgerService(new BroadcastService("http://localhost",4567), new KeyService());
		ledger.createTransaction("1", new BigDecimal(1000));
		ledger.createTransaction("2", new BigDecimal(0));
		ledger.createTransaction("1", "2", new BigDecimal(100));
		System.out.println(ledger.getBalance("1"));
		System.out.println(ledger.getBalance("2"));
		System.out.println(ledger.getMoneyInSystem());
	}

}
