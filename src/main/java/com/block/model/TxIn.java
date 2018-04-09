package com.block.model;

import java.io.Serializable;

public class TxIn  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private String txOutId;
//	private int txOutIndex;
	private String signature;
	private String description;
	private UnspentTxOut linkedUTxO;

	private TxIn(UnspentTxOut uTxO, String description) {
		super();
		this.linkedUTxO = uTxO;
//		this.txOutId = txOutId;
//		this.txOutIndex = txOutIndex;
		this.description = description;
	}
	
//	public static TxIn valueOf(String txOutId, int txOutIndex, String description) {
//		return new TxIn(txOutId, txOutIndex, description);
//	}

	public static TxIn valueOf(UnspentTxOut uTxO , String description) {
		return new TxIn(uTxO, description);
	}

	public String getTxOutId() {
		return linkedUTxO.getTxOutId();
	}

	public int getTxOutIndex() {
		return linkedUTxO.getTxOutIndex();
	}

	public String getSignature() {
		return signature;
	}

	public UnspentTxOut getLinkedUTxO() {
		return linkedUTxO;
	}

	@Override
	public String toString() {
		return "[ " + getTxOutId() + ", " + getTxOutIndex() + ", " + signature + "]";
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

}
