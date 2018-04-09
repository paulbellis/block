package com.block.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import com.block.commons.Hasher;
import com.block.crypto.Keys;

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

	public static Transaction valueOf(Keys key, List<TxIn> txIns, List<TxOut> txOuts) {
		if (txIns == null || txOuts == null || txOuts.isEmpty() || key == null) {
			return null;
		}
		Transaction tx = new Transaction(txIns, txOuts);
		byte[] signature = key.sign(tx.getId());
		for (TxIn txIn : txIns) {
			txIn.setSignature(Hex.encodeHexString(signature));
		}
		return tx;
	}

	public String hashTxs() {
		Optional<String> oTxIn = txIns.stream().map((TxIn txIn) -> txIn.getTxOutId() + String.valueOf(txIn.getTxOutIndex())).reduce((s1,s2) -> s1+s2);
		Optional<String> oTxOut = txOuts.stream().map((TxOut txOut) -> txOut.toString()).reduce((s1,s2) -> s1+s2);
		String txAsStringToHash = (oTxIn.isPresent()?oTxIn.get():"") + (oTxOut.isPresent()?oTxOut.get():"");
		byte[] doubleHash = Hasher.getHash256AsBytes(Hasher.getHash256AsBytes(txAsStringToHash.getBytes()));
		ArrayUtils.reverse(doubleHash);
		return Hex.encodeHexString(doubleHash);
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
		return new Transaction(new ArrayList<>(), txOuts);
	}

}
