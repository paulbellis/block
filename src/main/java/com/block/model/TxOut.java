package com.block.model;

import java.io.Serializable;
import java.math.BigDecimal;

public final class TxOut implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String address;
	private BigDecimal amount;
	private int index;

	private TxOut(String address, BigDecimal amount, int index) {
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
	
	public static TxOut valueOf(String address, BigDecimal amount, int index) {
		return new TxOut(address, amount, index);
	}
}
