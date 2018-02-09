package com.block.service;

import java.math.BigDecimal;

import com.block.commons.InsufficientFundsException;
import com.block.model.AccountTransfer;

public class TransferService {
	
	private DummyStore db;
	private Ledger ledger;

	public TransferService(DummyStore db, Ledger ledger) {
		super();
		this.db = db;
		this.ledger = ledger;
	}

	public synchronized void transfer(AccountTransfer transfer) throws InsufficientFundsException {
		String from = transfer.getFromAccountId();
		String to = transfer.getToAccountId();
		int amount = transfer.getAmount().intValue();
		db.transfer(from,to,amount,transfer.getHash());
		ledger.transfer(to, new BigDecimal(amount), from, transfer.getHash());
	}

}