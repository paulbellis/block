package com.block.commons;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import com.block.model.Transaction;

public class TestHasher {

	@Test
	public void testGetHash() {
		assertTrue(Hasher.getHash("hello")
				.equals("5d41402abc4b2a76b9719d911017c592"));
	}

	@Test(expected = NullPointerException.class)
	public void testGetHashWithNullInput() {
		Hasher.getHash(null);
	}

	@Test
	public void testCalculateHash() {
		assertTrue(Hasher
				.calculateHash(0, "previoushash", Instant.ofEpochMilli(1522225966), new ArrayList<Transaction>(), 0, 0)
				.equals("e96541ddc92ac9067fd640a77d483e69692c65a4fb8fed27cce79658da41d406"));
	}

	@Test
	public void testGetHash256OfBytes() {
		assertTrue(Hasher.getHash256OfBytes("hello".getBytes())
				.equals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
	}

	@Test(expected = NullPointerException.class)
	public void testGetHash256OfBytesWithNullInput() {
		Hasher.getHash256OfBytes(null);
	}

	@Test
	public void testGetHash256OfString() {
		assertTrue(Hasher.getHash256OfString("hello")
				.equals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
	}

	@Test(expected = NullPointerException.class)
	public void testGetHash256OfStringWithNullInput() {
		Hasher.getHash256OfString(null);
	}

	@Test
	public void testGetHash256AsBytes() {
		assertTrue(Hex.toHexString(Hasher.getHash256AsBytes("hello".getBytes())).equals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
	}

	@Test(expected = NullPointerException.class)
	public void testGetHash256AsBytesWithNullInput() {
		Hasher.getHash256AsBytes(null);
	}

}
