package com.block.model;

import java.math.BigDecimal;

public final class TxOut {

	private String address;
	private BigDecimal amount;
	private int index;

	public TxOut(String address, BigDecimal amount, int index) {
		super();
		this.address = address;
		this.amount = amount;
		this.index = index;
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return "[ " + address + "," + amount + "," + index + "]";
	}
}
