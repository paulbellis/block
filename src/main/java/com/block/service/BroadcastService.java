package com.block.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.block.commons.JSON;
import com.block.model.Transaction;

public class BroadcastService {
	
	private List<String> addresses = new ArrayList<>();
	private volatile static BroadcastService instance = null;
	
	public static BroadcastService getInstance() {
		if (instance==null) {
			synchronized(BroadcastService.class) {
				instance = new BroadcastService();
			}
		}
		return instance;
	}
	
	public void broadcastTransaction(Transaction t) {
		for (String address : addresses) {
			try {
				HttpService.doPost(address+"/transaction", JSON.toJson(t));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
