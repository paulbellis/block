package com.block.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpService {

	private static Logger log = LogManager.getLogger(HttpService.class);

	public static String getUri(String url) {
		URIBuilder uri = null;
		try {
			uri = new URIBuilder(url);
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		}
		return (uri==null?null:uri.toString());

	}

	public static String post(String url, String body) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost request = new HttpPost(getUri(url));

		StringEntity entity = new StringEntity(body);
		request.setEntity(entity);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");

		CloseableHttpResponse response = httpclient.execute(request);
		response = httpclient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		String responseString = EntityUtils.toString(responseEntity,"UTF-8");
		return responseString;
	}

	public static String get(String url) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet request = new HttpGet(HttpService.getUri(url));

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

}
