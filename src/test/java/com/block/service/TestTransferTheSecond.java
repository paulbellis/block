package com.block.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.block.commons.ClientCalls;
import com.block.commons.JSON;
import com.block.commons.Properties;
import com.block.model.AccountBalance;
import com.block.model.Block;
import com.block.model.UnspentTxOut;
import com.block.rest.Server;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TestTransferTheSecond {

	private static Logger log = LogManager.getLogger(TestTransferTheSecond.class);

	private List<String> createNAccounts(int num) throws ClientProtocolException, IOException {
		List<String> accountIds = new ArrayList<>();
		for (int i=1; i<num+1; i++) {
			BigDecimal balance = new BigDecimal(100);
			String accountId = String.valueOf(i);
			accountIds.add(accountId);
			AccountBalance ab1 = new AccountBalance(accountId, balance.intValue(),0,0);
			ClientCalls.createAccount(Properties.getCreateURL(), ab1);
		}
		return accountIds;
	}

	@Test
	public void testMultiTransfer() throws ClientProtocolException, IOException, InterruptedException {
		int accountCount = 100;
		Server server = new Server("http://localhost",4567);
		server.start();
		Thread.sleep(10000);
		List<String> accountIds = createNAccounts(accountCount);
		Thread.sleep(10000);

		ExecutorService service = Executors.newFixedThreadPool(10);
		for (int i=0; i<accountCount; i++) {
			String fromAccountId = accountIds.get(i);
			for (int j=0; j<accountCount; j++) {
				String toAccountId = accountIds.get(j);
				if (!fromAccountId.equals(toAccountId)) {
					TransferRunner fromMeToYou = new TransferRunner(fromAccountId, toAccountId);
					service.submit(fromMeToYou);
					log.debug("will deliver from " + fromAccountId + " to " + toAccountId);
				}
			}
		}
		
		service.shutdown();
		try {
			service.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

		String bcJson = ClientCalls.dumpLedger(Properties.getLedgerDumpURL());
		log.info(bcJson);
		List<Block> bc = JSON.fromJsonToList(bcJson, new TypeToken<List<Block>>(){}.getType());
		Ledger testLedger = new Ledger(bc);

		String dbJson =  ClientCalls.dumpDatastore(Properties.getDatastoreDumpURL());
		Type mapType = new TypeToken<Map<String, AccountBalance>>(){}.getType();  
		Map<String, AccountBalance> m = new Gson().fromJson(dbJson, mapType);

		for (String id : accountIds) {
			List<UnspentTxOut> uTxOs = testLedger.getUnspentTxOutForAccount(id);
			int total = 0;
			for (UnspentTxOut uTx : uTxOs) {
				total = total + uTx.getAmount().intValue();
			}
			assertTrue(m.get(id).getBalance()==total);
			log.info(id + "," + total + "," +  uTxOs);
		}
		log.info(m);
		server.stopServer();
	}

}
