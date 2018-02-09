package com.block.model;

public class TxIn {

	private String txOutId;
	private int txOutIndex;
	private String signature;

	public TxIn(String txOutId, int txOutIndex, String signature) {
		super();
		this.txOutId = txOutId;
		this.txOutIndex = txOutIndex;
		this.signature = signature;
	}

	public String getTxOutId() {
		return txOutId;
	}

	public int getTxOutIndex() {
		return txOutIndex;
	}

	public String getSignature() {
		return signature;
	}

	@Override
	public String toString() {
		return "[ " + txOutId + ", " + txOutIndex + ", " + signature + "]";
	}

}
