package com.block.commons;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestUrlBuilder {
	
	private static String host = "xxx";
	private static int port = 999;
	private static String baseUrl = "http://xxx:999";

	@Test
	public void testCreateStringInt() {
		assertTrue(URLBuilder.create(host, port).equals(baseUrl+"/create"));
	}

	@Test
	public void testDumpStringInt() {
		assertTrue(URLBuilder.dump(host, port).equals(baseUrl+"/dump"));
	}

	@Test
	public void testLedgerStringInt() {
		assertTrue(URLBuilder.ledger(host, port).equals(baseUrl+"/ledger"));
	}

	@Test
	public void testTransferStringInt() {
		assertTrue(URLBuilder.transfer(host, port).equals(baseUrl+"/transfer"));
	}

	@Test
	public void testBlock() {
		assertTrue(URLBuilder.block(baseUrl).equals(baseUrl+"/block"));
	}

	@Test
	public void testCreateString() {
		assertTrue(URLBuilder.create(baseUrl).equals(baseUrl+"/create"));
	}

	@Test
	public void testDumpString() {
		assertTrue(URLBuilder.dump(baseUrl).equals(baseUrl+"/dump"));
	}

	@Test
	public void testLedgerString() {
		assertTrue(URLBuilder.ledger(baseUrl).equals(baseUrl+"/ledger"));
	}

	@Test
	public void testTransferString() {
		assertTrue(URLBuilder.transfer(baseUrl).equals(baseUrl+"/transfer"));
	}

	@Test
	public void testStats() {
		assertTrue(URLBuilder.stats(baseUrl).equals(baseUrl+"/stats"));
	}

}
