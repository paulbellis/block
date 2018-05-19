package com.block.crypto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class TestECDSA {
    @Test
    public void verify() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        String data = "hello world";
        byte[] sig = ecdsa.sign(data);
        assertTrue(ecdsa.verify(data, sig));

    }

    @Test
    public void verify1() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        String data = "hello world";
        byte[] sig = ecdsa.sign(data);
        assertTrue(ECDSA.verify(ecdsa.recreatePubKeyFromAddress(ecdsa.getPublicKeyDecodedHexAddress()), data, sig));
        assertFalse(ECDSA.verify(ecdsa.recreatePubKeyFromAddress(ecdsa.getPublicKeyDecodedHexAddress()), data + "x", sig));
    }

    @Test
    public void sign() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        String data = "hello world";
        byte[] sig = ecdsa.sign(data);
        assertTrue(ecdsa.verify(data, sig));
    }

    @Test
    public void getPubFromPriv() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        assertTrue(ecdsa.getPubFromPriv() instanceof PublicKey);
    }

    @Test
    public void getPriv() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        assertTrue(ecdsa.getPriv() instanceof PrivateKey);
    }

    @Test
    public void getPub() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        assertTrue(ecdsa.getPub() instanceof PublicKey);
    }

    @Test
    public void init() throws Exception {
        ECDSA ecdsa = new ECDSA();
        assertTrue(ecdsa.getPub() == null);
        ecdsa.init("localhost4567");
        assertTrue(ecdsa.getPub() instanceof PublicKey);
    }

    @Test
    public void recreatePubKeyFromAddress() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        String o = ecdsa.getPublicKeyDecodedHexAddress();
        String n  = Hex.encodeHexString(ecdsa.recreatePubKeyFromAddress(o).getEncoded());
        assertTrue(o.equals(n));
    }

    @Test
    public void getPubKeyFromAddress() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        String o = ecdsa.getPublicKeyDecodedHexAddress();
        String n  = Hex.encodeHexString(ECDSA.getPubKeyFromAddress(o).getEncoded());
        assertTrue(o.equals(n));
    }

    @Test
    public void valueOf() throws Exception {
    	assertTrue(ECDSA.valueOf() instanceof ECDSA);
    }

    @Test
    public void getPublicKeyDecodedHexAddress() throws Exception {
        ECDSA ecdsa = new ECDSA();
        ecdsa.init("localhost4567");
        assertTrue(ecdsa.getPublicKeyDecodedHexAddress().length()==182);
    }

}