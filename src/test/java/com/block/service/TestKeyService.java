package com.block.service;

import java.io.IOException;

import org.junit.Test;

public class TestKeyService {


	@Test
	public void testGetPublicKeyString() {
		KeyService cryptoService = new KeyService();
		cryptoService.addNodeKey("localhost4567");
		System.out.println(cryptoService.getNodePublicKey());
	}

	@Test
	public void testInit() throws IOException {
		KeyService keyService = new KeyService();
		keyService.init();
	}

}
