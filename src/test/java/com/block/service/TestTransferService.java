package com.block.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.block.commons.ClientCalls;
import com.block.commons.Properties;
import com.block.model.AccountBalance;
import com.block.model.AccountTransfer;
import com.block.rest.Server;

public class TestTransferService {

	private int id = 1;

	private void createAccount() throws ClientProtocolException, IOException {
		BigDecimal balance = new BigDecimal(100);
		String accountId = String.valueOf(id++);
		AccountBalance ab1 = new AccountBalance(accountId, balance.intValue(), 0, 0);
		ClientCalls.createAccount(Properties.getCreateURL(), ab1);
	}

	@Test
	public void test() throws ClientProtocolException, IOException, InterruptedException {
		Server.start(4567);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		for (int i = 1; i < 3; i++) {
			createAccount();
		}

		AccountTransfer t1 = AccountTransfer.valueOf("1", "2", new BigDecimal(33));
		try {
			ClientCalls.sendTransferRequest(Properties.getTransferURL(), t1);
		} 
		catch (ClientProtocolException e) {
		} 
		catch (IOException e) {
		}

		Server.stopServer();
	}

}
