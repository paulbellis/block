package com.block.service;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.client.ClientProtocolException;

import com.block.commons.JSON;
import com.block.commons.URLBuilder;
import com.block.model.AccountTransfer;

public class TestTransferService {

	private int id = 1;

	public void test() throws ClientProtocolException, IOException, InterruptedException {
		AccountTransfer t1 = AccountTransfer.valueOf("1234", "1", new BigDecimal(10));
		System.out.println(t1);
		try {
			HttpService.post(URLBuilder.transfer("http://localhost",4567), JSON.toJson(t1));
		} 
		catch (ClientProtocolException e) {
		} 
		catch (IOException e) {
		}

//		server1.stopServer();
//		server2.stopServer();
//		server3.stopServer();
	}

}
