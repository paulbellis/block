package com.block.commons;

public class Properties {
	public static String BASE_URL = "localhost";
	public static String PROTOCOL = "http";
	public static String PORT = "4567";
	
	
	public static String getCreateURL() {
		return PROTOCOL+"://"+BASE_URL+":"+PORT+"/create";
	}


	public static String getDatastoreDumpURL() {
		return PROTOCOL+"://"+BASE_URL+":"+PORT+"/dump";
	}
	
	public static String getLedgerDumpURL() {
		return PROTOCOL+"://"+BASE_URL+":"+PORT+"/ledger";
	}
	
	public static String getTransferURL() {
		return PROTOCOL+"://"+BASE_URL+":"+PORT+"/transfer";
	}
	
}
