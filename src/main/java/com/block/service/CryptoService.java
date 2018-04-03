package com.block.service;

import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import com.block.crypto.Cryptographies;
import com.block.crypto.RSA;

public class CryptoService {

	private Cryptographies nodekey;
	private Map<String,Cryptographies> keys;
	
	public CryptoService() {
	}
	
	public void addNodeKey(String nodename) {
		Cryptographies nodekey = RSA.valueOf();
		nodekey.init(nodename);
		this.nodekey = nodekey;
	}
	
	public String getNodePublicKey() {
		return Hex.encodeHexString(nodekey.getPub().getEncoded());
	}
}
