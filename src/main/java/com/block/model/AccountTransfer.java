package com.block.model;

import java.math.BigDecimal;
import java.util.Random;

import com.block.commons.Hasher;

public final class AccountTransfer {

	private String fromAccountId;
	private String toAccountId;
	private BigDecimal amount;
	private String hash;

	private AccountTransfer(String fromAccountId, String toAccountId, BigDecimal amount) {
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
		Random r = new Random();
		this.hash = Hasher.getHash(fromAccountId + toAccountId + amount.toString()+r.nextInt(1000000));
	}

	public static AccountTransfer valueOf(String fromAccountId, String toAccountId, BigDecimal amount) {
		return new AccountTransfer(fromAccountId, toAccountId, amount);
	}

	public String getFromAccountId() {
		return fromAccountId;
	}

	public String getToAccountId() {
		return toAccountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getHash() {
		return hash;
	}
	
	public String toString() {
		return "[" + fromAccountId + "," + toAccountId + "," + amount + "," + hash + "]";
	}
}
