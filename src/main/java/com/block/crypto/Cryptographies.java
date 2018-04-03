package com.block.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public interface Cryptographies {

	boolean verify(String data, byte[] sig);

	byte[] sign(String data);

	PrivateKey getPriv();

	PublicKey getPub();
	
	void init(String users);

	public PublicKey getPubFromPriv() throws NoSuchAlgorithmException, InvalidKeySpecException;

}