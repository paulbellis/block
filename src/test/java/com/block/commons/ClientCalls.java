package com.block.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientCalls {
	private static Logger log = LogManager.getLogger(ClientCalls.class);
	

//	public static void createAccount(String url, AccountBalance ab) throws ClientProtocolException, IOException {
//		HttpService.post(url,JSON.toJson(ab));
//	}
//
//	public static void sendTransferRequest(String url, AccountTransfer t1) throws ClientProtocolException, IOException {
//		CloseableHttpClient httpclient = HttpClients.createDefault();
//		HttpPut request = new HttpPut(HttpService.getUri(url));
//
//		String json = JSON.toJson(t1);
//		StringEntity entity = new StringEntity(json);
//		request.setEntity(entity);
//		request.setHeader("Accept", "application/json");
//		request.setHeader("Content-type", "application/json");
//
//		CloseableHttpResponse response = httpclient.execute(request);
//	}
//	
//
//	public static String dumpLedger(String url) {
//		return HttpService.get(url);
//	}
//
//	public static String dumpDatastore(String url) {
//		return HttpService.get(url);
//	}
//
//	public static String getBalance(String url) {
//		return HttpService.get(url);
//	}

}
