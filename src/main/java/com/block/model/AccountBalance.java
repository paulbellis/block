package com.block.model;

public class AccountBalance {
	private String accountId;
	private int balance;
	private int ins;
	private int outs;
	
	public AccountBalance(String accountId, int balance) {
		super();
		this.accountId = accountId;
		this.balance = balance;
	}

	
	
	public String getAccountId() {
		return accountId;
	}



	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}



	public int getBalance() {
		return balance;
	}



	public void setBalance(int balance) {
		this.balance = balance;
	}



	public int getIns() {
		return ins;
	}



	public void setIns(int ins) {
		this.ins = ins;
	}



	public int getOuts() {
		return outs;
	}



	public void setOuts(int outs) {
		this.outs = outs;
	}



	public String toString() {
		return "[ id " + accountId + ", balance " + balance + ", ins " + ins + ", outs " + outs +"]";
	}
}
