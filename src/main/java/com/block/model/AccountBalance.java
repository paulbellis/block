package com.block.model;

public class AccountBalance {
	private String accountId;
	private int balance;
	private int ins;
	private int outs;


	public AccountBalance(String accountId, int balance, int ins, int outs) {
		super();
		this.accountId = accountId;
		this.balance = balance;
		this.ins = ins;
		this.outs = outs;
	}

	public String getAccountId() {
		return accountId;
	}

	public int getBalance() {
		return balance;
	}

	public int getIns() {
		return ins;
	}

	public int getOuts() {
		return outs;
	}


	public String toString() {
		return "[ id " + accountId + ", balance " + balance + ", ins " + ins + ", outs " + outs +"]";
	}


	public static AccountBalance createEmptyBalance(String accountId) {
		return new AccountBalance(accountId, 0, 0, 0);
	}
}
