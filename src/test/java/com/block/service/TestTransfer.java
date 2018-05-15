package com.block.service;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;

import com.block.rest.Server;

public class TestTransfer {

	private int id = 1;

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
