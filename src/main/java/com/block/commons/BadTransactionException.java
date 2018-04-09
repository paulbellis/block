package com.block.commons;

public class BadTransactionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BadTransactionException() {
	}

	public BadTransactionException(String string) {
		super(string);
	}

}
