package com.block.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;

public interface Keys {

	boolean verify(String data, byte[] sig);

	byte[] sign(String data);

	PrivateKey getPriv();

	PublicKey getPub();
	
	void init(String users);

	public PublicKey getPubFromPriv() throws NoSuchAlgorithmException, InvalidKeySpecException;
	
	public PublicKey recreatePubKeyFromAddress(String hexEncodedBase64DecodedPemPublicKeyText) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, DecoderException;

	String getPublicKeyDecodedHexAddress();


}