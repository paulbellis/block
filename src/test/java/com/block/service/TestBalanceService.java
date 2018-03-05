package com.block.service;

import static org.junit.Assert.assertTrue;

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
import com.block.rest.Server;

public class TestBalanceService {

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createAccount();
		String balance = ClientCalls.getBalance(Properties.getBalanceURL("1"));
		assertTrue(Integer.valueOf(balance)==100);
		System.out.println(balance);
		Server.stopServer();
	}

}
