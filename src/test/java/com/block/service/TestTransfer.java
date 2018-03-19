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
import com.block.rest.Server;

public class TestTransfer {

	private int id = 1;

	private void createAccount() throws ClientProtocolException, IOException {
		BigDecimal balance = new BigDecimal(100);
		String accountId = String.valueOf(id++);
		AccountBalance ab1 = new AccountBalance(accountId, balance.intValue(),0,0);
		ClientCalls.createAccount(Properties.getCreateURL(), ab1);
	}
	
	@Test
	public void test() throws ClientProtocolException, IOException, InterruptedException {
		Server server = new Server("http://localhost",4567);
		server.start();
	
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i=0;i<100;i++) {
			createAccount();
		}

		Random r = new Random();
		ExecutorService service = Executors.newFixedThreadPool(1);
		for (int i=0; i<1000; i++ ) {
			Future<?> f = service.submit(new TransferCallable(String.valueOf(r.nextInt(100)), String.valueOf(r.nextInt(100)), r.nextInt(100)));
		}
//		try {
//			f.get();
//		} catch (InterruptedException | ExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		service.shutdown();
		service.awaitTermination(1,TimeUnit.DAYS);
//		Scanner scanner = new Scanner(System.in);
//		scanner.nextLine();
//		scanner.close();
		server.stopServer();
	}

}
