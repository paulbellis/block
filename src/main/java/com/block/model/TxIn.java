package com.block.model;

import java.io.Serializable;

public class TxIn  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
