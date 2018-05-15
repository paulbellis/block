package com.block.service;

import java.math.BigDecimal;

import com.block.commons.InsufficientFundsException;
import com.block.model.AccountTransfer;

public class TransferService {
	
	private Ledgers ledger;

	public static Object transfer(Ledgers ledger, AccountTransfer transfer) throws InsufficientFundsException {
		
		String from = transfer.getFromAccountId();
		String to = transfer.getToAccountId();
		int amount = transfer.getAmount().intValue();
		try {
			ledger.createTransaction(from, to, new BigDecimal(amount));
		}
		catch (InsufficientFundsException e) {
			return "Not enough funds to transfer";
		}
		return "Success";
	}

}