package com.block.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

import com.block.commons.Hasher;

public class Transaction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int COINBASE_AMOUNT = 50;
	private String id;
	private List<TxIn> txIns;
	private List<TxOut> txOuts;
	private String description;

	public String toString() {
		return "[" + id +"," + txIns +"," + txOuts + "," + description + "]";
	}
	
	private Transaction(List<TxIn> txIns, List<TxOut> txOuts) {
		super();
		this.txIns = txIns;
		this.txOuts = txOuts;
		this.id = hashTxs();
	}

	public static Transaction valueOf(List<TxIn> txIns, List<TxOut> txOuts) {
		if (txIns == null || txOuts == null || txOuts.isEmpty()) {
			return null;
		}
		return new Transaction(txIns, txOuts);
	}

	private String hashTxs() {
		byte[] doubleHash = Hasher.getHash256AsBytes(Hasher.getHash256AsBytes(SerializationUtils.serialize(this)));
		ArrayUtils.reverse(doubleHash);
		return Hex.encodeHexString(doubleHash);
//		this.toString().getBytes()
//		StringBuilder sb = new StringBuilder();
//		for (TxIn txIn : txIns) {
//			sb.append(txIn.getSignature()).append(txIn.getTxOutId()).append(txIn.getTxOutIndex());
//		}
//		for (TxOut txOut : txOuts) {
//			sb.append(txOut.getAddress()).append(txOut.getAmount());
//		}
//		return Hasher.getHash(sb.toString());
	}

	public String getId() {
		return id;
	}

	public List<TxIn> getTxIns() {
		return txIns;
	}

	public List<TxOut> getTxOuts() {
		return txOuts;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static Transaction createCoinBase(String address) {
		TxOut coinBase = TxOut.valueOf(address, new BigDecimal(COINBASE_AMOUNT), 0);
		List<TxOut> txOuts = new ArrayList<>();
		txOuts.add(coinBase);
		return Transaction.valueOf(new ArrayList<>(), txOuts);
	}

}
