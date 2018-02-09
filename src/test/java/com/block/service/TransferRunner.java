package com.block.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.commons.ClientCalls;
import com.block.commons.Properties;
import com.block.model.AccountTransfer;

public class TransferRunner implements Runnable {

	private static Logger log = LogManager.getLogger(TransferRunner.class);
	
	private String from;
	private String to;
	
	public TransferRunner(String from, String to) {
		super();
		this.from = from;
		this.to = to;
	}

	@Override
	public void run() {
		Random r = new Random();
		int paidIn = 0;
		int paidOut = 0;
		for (int i=0; i<100; i++) {
			int amount = r.nextInt(10)+1;
			AccountTransfer t1 = AccountTransfer.valueOf(from, to, new BigDecimal(amount));
			try {
				log.debug("Sending " + t1);
				ClientCalls.sendTransferRequest(Properties.getTransferURL(),t1);
			} catch (ClientProtocolException e) {
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}

}
