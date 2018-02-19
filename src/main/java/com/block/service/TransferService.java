package com.block.service;

import java.math.BigDecimal;

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;
import com.block.model.AccountTransfer;
import com.block.model.Ledger1;

public class TransferService {
	
	private DummyStore db;
	private Ledger1 ledger;

	public TransferService(DummyStore db, Ledger1 ledger) {
		super();
		this.db = db;
		this.ledger = ledger;
	}

	public synchronized void transfer(AccountTransfer transfer) throws InsufficientFundsException, AccountNotExistException {
		String from = transfer.getFromAccountId();
		String to = transfer.getToAccountId();
		int amount = transfer.getAmount().intValue();
		db.transfer(from,to,amount,transfer.getHash());
		ledger.createTransaction(from, to, new BigDecimal(amount));
	}

}