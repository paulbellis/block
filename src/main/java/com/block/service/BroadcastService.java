package com.block.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.block.commons.JSON;
import com.block.model.Block;
import com.block.model.Transaction;
import com.google.gson.reflect.TypeToken;

public class BroadcastService {
	
	private Set<String> nodeList = new ConcurrentSkipListSet<>();
	private static volatile BroadcastService instance = null;
	private String thisNodeUrl;
	private int thisNodePort;
	public static String THIS_SERVER = null;
	
	public BroadcastService(String url, int port) {
		thisNodeUrl = url;
		thisNodePort = port;
		THIS_SERVER = thisNodeUrl +":" + thisNodePort;
	}

	public void broadcastTransaction(Transaction t) {
		for (String address : nodeList) {
			try {
				HttpService.doPost(address+"/transaction", JSON.toJson(t));
			} catch (IOException e) {
			}
		}
	}
	
	public void broadcastBlock(Block b) {
		for (String address : nodeList) {
			try {
				HttpService.doPost(address+"/block", JSON.toJson(b));
			} catch (IOException e) {
			}
		}
	}
	
	public void addAddress(String address) {
		synchronized(nodeList) {
			nodeList.add(address);
		}
	}

	public void addAddresses(List<String> nodes) {
		for (String address : nodes) {
			if (!address.equals(THIS_SERVER)) {
				synchronized(nodeList) {
					if (!nodeList.contains(address)) {
						nodeList.add(address);
					}
				}
			}
		}
	}

	public String getAddresses() {
		return JSON.toJson(nodeList);
	}

	public void getNetworkNodes() {
		for (String address : nodeList) {
			List<String> newNodes = JSON.fromJsonToList(HttpService.get(address+"/servers"),new TypeToken<List<String>>(){}.getType());
			addAddresses(newNodes);
		}
	}
	
	public void broadCastMe() {
		for (String address : nodeList) {
			
			try {
				List<String> nodeListAndThisHost = new ArrayList<>();
				nodeListAndThisHost.addAll(nodeList);
				nodeListAndThisHost.add(THIS_SERVER);
				HttpService.doPost(address+"/servers", JSON.toJson(nodeListAndThisHost));
			} catch (IOException e) {
			}
		}
	}

}
