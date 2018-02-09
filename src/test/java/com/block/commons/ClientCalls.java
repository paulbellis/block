package com.block.commons;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.model.AccountBalance;
import com.block.model.AccountTransfer;

public class ClientCalls {
	private static Logger log = LogManager.getLogger(ClientCalls.class);
	
	private static String getUri(String url) {
		URIBuilder uri = null;
		try {
			uri = new URIBuilder(url);
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		}
		return (uri==null?null:uri.toString());

	}

	public static void createAccount(String url, AccountBalance ab) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost request = new HttpPost(getUri(url));

		String json = JSON.toJson(ab);
		StringEntity entity = new StringEntity(json);
		request.setEntity(entity);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");

		CloseableHttpResponse response = httpclient.execute(request);
	}

	public static void sendTransferRequest(String url, AccountTransfer t1) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPut request = new HttpPut(getUri(url));

		String json = JSON.toJson(t1);
		StringEntity entity = new StringEntity(json);
		request.setEntity(entity);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");

		CloseableHttpResponse response = httpclient.execute(request);
	}
	
	private static String get(String url) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet request = new HttpGet(getUri(url));

		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");

		CloseableHttpResponse response =null;
		try {
			response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity,"UTF-8");
			return responseString;
		} catch (ClientProtocolException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public static String dumpLedger(String url) {
		return get(url);
	}

	public static String dumpDatastore(String url) {
		return get(url);
	}

}
