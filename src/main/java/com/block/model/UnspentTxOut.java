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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((txOutId == null) ? 0 : txOutId.hashCode());
		result = prime * result + txOutIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnspentTxOut other = (UnspentTxOut) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (txOutId == null) {
			if (other.txOutId != null)
				return false;
		} else if (!txOutId.equals(other.txOutId))
			return false;
		if (txOutIndex != other.txOutIndex)
			return false;
		return true;
	}

}
