package com.block.service;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.block.rest.Api;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.block.rest.Server;

public class TestTransferDaThird {

	private static Logger log = LogManager.getLogger(TestTransferDaThird.class);

	public void testMultiTransfer() throws ClientProtocolException, IOException, InterruptedException {
		int accountCount = 100;
		Api server = new Api();
		server.start("http://localhost",4567,"paul", "config.txt");


		ExecutorService service = Executors.newFixedThreadPool(10);
		//service.submit(new TransferCallable("35","67",0));

		Random r = new Random();
//		while (true) {
//			TransferCallable tc = new TransferCallable(String.valueOf(r.nextInt(accountCount)), String.valueOf(r.nextInt(accountCount)), r.nextInt(10));
//			service.submit(tc);
//			Thread.sleep(250);
//		}
		for (int i=0; i<10000; i++) {
			TransferCallable tc = new TransferCallable(String.valueOf(r.nextInt(accountCount)), String.valueOf(r.nextInt(accountCount)), r.nextInt(10));
			service.submit(tc);
		}
		service.shutdown();
		try {
			service.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

//		String bcJson = ClientCalls.dumpLedger(Properties.getLedgerDumpURL());
//		log.info(bcJson);
//		List<Block> bc = JSON.fromJsonToList(bcJson, new TypeToken<List<Block>>(){}.getType());
//		Ledger testLedger = new Ledger(bc);
//
//		String dbJson =  ClientCalls.dumpDatastore(Properties.getDatastoreDumpURL());
//		Type mapType = new TypeToken<Map<String, AccountBalance>>(){}.getType();  
//		Map<String, AccountBalance> m = new Gson().fromJson(dbJson, mapType);
//
//		for (String id : accountIds) {
//			List<UnspentTxOut> uTxOs = testLedger.getUnspentTxOutForAccount(id);
//			int total = 0;
//			for (UnspentTxOut uTx : uTxOs) {
//				total = total + uTx.getAmount().intValue();
//			}
//			assertTrue(m.get(id).getBalance()==total);
//			log.info(id + "," + total + "," +  uTxOs);
//		}
//		log.info(m);

		server.stopServer();
	}

}
