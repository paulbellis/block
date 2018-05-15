package com.block.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.block.commons.URLBuilder;
import com.block.rest.Server;

public class TestBalanceService {

	private int id = 1;
	
	@Test
	public void test() throws ClientProtocolException, IOException, InterruptedException {
		Server server = new Server();
		server.init("http://localhost",4567,"paul", "config.txt");
		server.start();
	
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String balance = HttpService.get(URLBuilder.balance("http://localhost",4567,"1"));
		assertTrue(balance==null);
		System.out.println(balance);
		server.stopServer();
	}

}
