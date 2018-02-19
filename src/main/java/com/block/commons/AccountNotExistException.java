package com.block.commons;

public class AccountNotExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccountNotExistException() {
	}

	public AccountNotExistException(String string) {
		super(string);
	}

}
