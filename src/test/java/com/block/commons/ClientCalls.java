package com.block.commons;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.model.AccountBalance;
import com.block.model.AccountTransfer;
import com.block.service.HttpService;

public class ClientCalls {
	private static Logger log = LogManager.getLogger(ClientCalls.class);
	

	public static void createAccount(String url, AccountBalance ab) throws ClientProtocolException, IOException {
		HttpService.doPost(url,JSON.toJson(ab));
	}

	public static void sendTransferRequest(String url, AccountTransfer t1) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPut request = new HttpPut(HttpService.getUri(url));

		String json = JSON.toJson(t1);
		StringEntity entity = new StringEntity(json);
		request.setEntity(entity);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");

		CloseableHttpResponse response = httpclient.execute(request);
	}
	

	public static String dumpLedger(String url) {
		return HttpService.get(url);
	}

	public static String dumpDatastore(String url) {
		return HttpService.get(url);
	}

	public static String getBalance(String url) {
		return HttpService.get(url);
	}

}
