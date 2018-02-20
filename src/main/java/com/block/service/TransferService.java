package com.block.service;

import java.math.BigDecimal;

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;
import com.block.model.AccountBalance;
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
		if (from.equals(to) || amount<0) {
			throw new IllegalArgumentException("xxx");
		}
		db.transfer(from,to,amount,transfer.getHash());
		ledger.createTransaction(from, to, new BigDecimal(amount));
		if (ledger.getBalance(from).compareTo(new BigDecimal(db.get(from).getBalance()))!=0) {
			BigDecimal bd1 = ledger.getBalance(from);
			AccountBalance b = db.get(from);
			
			System.out.println("Error in balances" + bd1+b);
			System.exit(1);
		}
		if (ledger.getBalance(to).compareTo(new BigDecimal(db.get(to).getBalance()))!=0) {
			BigDecimal bd1 = ledger.getBalance(to);
			AccountBalance b = db.get(to);
			
			System.out.println("Error in balances" + bd1+b);
			System.exit(1);
		}
	}

}