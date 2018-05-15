package com.block.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

import org.apache.http.client.ClientProtocolException;

import com.block.commons.JSON;
import com.block.commons.URLBuilder;
import com.block.model.AccountTransfer;

public class TransferCallable implements Callable<Integer> {

	private String from;
	private String to;
	private int amount;
	
	public TransferCallable(String from, String to, int amount) {
		super();
		this.from = from;
		this.to = to;
		this.amount = amount;
	}

	@Override
	public Integer call() throws Exception {
		AccountTransfer t1 = AccountTransfer.valueOf(from, to, new BigDecimal(amount));
		System.out.println("t,"+from+","+to+","+amount);
		try {
			HttpService.post(URLBuilder.transfer("http://localhost",4567),JSON.toJson(t1));
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		return null;
	}

}
