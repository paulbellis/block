package com.block.commons;

import org.apache.commons.codec.digest.DigestUtils;

public class Hasher {
	private Hasher() {
	}

	public static String getHash(String str) {
		return String.valueOf(DigestUtils.md5Hex(str));
	}
}
