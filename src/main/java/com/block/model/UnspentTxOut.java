package com.block.model;

import java.math.BigDecimal;

public class UnspentTxOut {
	private String txOutId;
	private int txOutIndex;
	private String address;
	private BigDecimal amount;

	public UnspentTxOut(String txOutId, int txOutIndex, String address, BigDecimal amount) {
		super();
		this.txOutId = txOutId;
		this.txOutIndex = txOutIndex;
		this.address = address;
		this.amount = amount;
	}

	public String getTxOutId() {
		return txOutId;
	}

	public int getTxOutIndex() {
		return txOutIndex;
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "[ id:" + txOutId + ", idx:" + txOutIndex + ", ad:" + address + ", am:" + amount + "]";
	}

}
