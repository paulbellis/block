package com.block.commons;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;

import org.junit.Test;

import com.block.model.Transaction;

public class TestHasher {

	@Test
	public void testGetHash() {
		assertTrue(Hasher.getHash("hello").equals("5d41402abc4b2a76b9719d911017c592"));
	}

	@Test(expected=NullPointerException.class)
	public void testGetHashWithNullInput() {
		Hasher.getHash(null);
	}

	@Test
	public void testCalculateHash() {
		assertTrue(Hasher.calculateHash(0, "previoushash", Instant.ofEpochMilli(1522225966), new ArrayList<Transaction>(), 0, 0).equals("e96541ddc92ac9067fd640a77d483e69692c65a4fb8fed27cce79658da41d406"));	
	}

	@Test
	public void testGetHash256() {
		assertTrue(Hasher.getHash256("hello".getBytes()).equals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
	}

}
