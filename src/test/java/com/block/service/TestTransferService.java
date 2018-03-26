package com.block.service;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.block.commons.ClientCalls;
import com.block.commons.Properties;
import com.block.model.AccountBalance;
import com.block.model.AccountTransfer;

public class TestTransferService {

	private int id = 1;

	private void createAccount(int initialAmount) throws ClientProtocolException, IOException {
		BigDecimal balance = new BigDecimal(initialAmount);
		String accountId = String.valueOf(id++);
		AccountBalance ab1 = new AccountBalance(accountId, balance.intValue(), 0, 0);
		ClientCalls.createAccount(Properties.getCreateURL(), ab1);
	}

	private void createAccount(String id, int initialAmount) throws ClientProtocolException, IOException {
		BigDecimal balance = new BigDecimal(initialAmount);
		AccountBalance ab1 = new AccountBalance(id, balance.intValue(), 0, 0);
		ClientCalls.createAccount(Properties.getCreateURL(), ab1);
	}

	@Test
	public void test() throws ClientProtocolException, IOException, InterruptedException {
//		Server server1 = new Server("http://localhost",4567);
//		server1.init("config2.txt");
//		server1.start();
//		Server server2 = new Server("http://localhost",4568);
//		server2.start();
//		Server server3 = new Server("http://localhost",4569);
//		server3.start();

//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//		}
//		for (int i = 1; i < 3; i++) {
//			createAccount();
//		}

		createAccount("1234",100);
		createAccount("1",0);
		AccountTransfer t1 = AccountTransfer.valueOf("1234", "1", new BigDecimal(10));
		System.out.println(t1);
		try {
			ClientCalls.sendTransferRequest(Properties.getTransferURL(), t1);
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
