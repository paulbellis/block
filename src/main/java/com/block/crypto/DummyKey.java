package com.block.crypto;

import org.apache.commons.codec.DecoderException;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class DummyKey implements Keys {
    @Override
    public boolean verify(String data, byte[] sig) {
        return false;
    }

    @Override
    public byte[] sign(String data) {
        return "signed".getBytes();
    }

    @Override
    public PrivateKey getPriv() {
        return null;
    }

    @Override
    public PublicKey getPub() {
        return null;
    }

    @Override
    public void init(String users) {

    }

    @Override
    public PublicKey getPubFromPriv() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return null;
    }

    @Override
    public PublicKey recreatePubKeyFromAddress(String hexEncodedBase64DecodedPemPublicKeyText) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, DecoderException {
        return null;
    }

    @Override
    public String getPublicKeyDecodedHexAddress() {
        return null;
    }
}
