package com.block.service;

import org.junit.Test;

public class TestCryptoService {

	@Test
	public void testGetPublicKeyString() {
		CryptoService cryptoService = new CryptoService();
		cryptoService.addNodeKey("localhost4567");
		System.out.println(cryptoService.getNodePublicKey());
	}

}
