package com.block.commons;

public class URLBuilder {
	public static String PROTOCOL = "http";
	
	private static String baseUrl(String host, int port) {
		return PROTOCOL+"://"+host+":"+port;
	}
	
	public static String create(String host, int port) {
		return baseUrl(host, port)+"/create";
	}

	public static String dump(String host, int port) {
		return baseUrl(host, port)+"/dump";
	}
	
	public static String ledger(String host, int port) {
		return baseUrl(host, port)+"/ledger";
	}
	
	public static String transfer(String host, int port) {
		return baseUrl(host, port)+"/transfer";
	}
	
	public static String balance(String host, int port, String id) {
		return baseUrl(host, port)+"/balance/"+id;
	}

	//////////////
	public static String block(String url) {
		return url+"/block";
	}

	public static String create(String url) {
		return url+"/create";
	}

	public static String dump(String url) {
		return url+"/dump";
	}
	
	public static String ledger(String url) {
		return url+"/ledger";
	}
	
	public static String transfer(String url) {
		return url+"/transfer";
	}
	
	public static String stats(String url) {
		return url+"/stats";
	}
	
}
