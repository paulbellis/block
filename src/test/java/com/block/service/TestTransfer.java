package com.block.service;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.block.rest.Api;
import org.apache.http.client.ClientProtocolException;

import com.block.rest.Server;

public class TestTransfer {

	private int id = 1;

	public void test() throws ClientProtocolException, IOException, InterruptedException {
		Api server = new Api();
		server.start("http://localhost",4567,"paul", "config.txt");

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
