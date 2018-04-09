package com.block.service;

import org.junit.Test;

public class TestCryptoService {

	@Test
	public void testGetPublicKeyString() {
		KeyService cryptoService = new KeyService();
		cryptoService.addNodeKey("localhost4567");
		System.out.println(cryptoService.getNodePublicKey());
	}

}
