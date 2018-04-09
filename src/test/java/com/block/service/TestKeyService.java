package com.block.service;

import java.io.IOException;

import org.junit.Test;

public class TestKeyService {

	@Test
	public void testInit() throws IOException {
		KeyService keyService = new KeyService();
		keyService.init();
	}

}
