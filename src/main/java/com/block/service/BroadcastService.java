package com.block.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.block.commons.JSON;
import com.block.commons.RemoteNodeBCandTP;
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
				String url = address+"/block?server=";
				HttpService.doPost(url + URLEncoder.encode(THIS_SERVER,StandardCharsets.UTF_8.toString()), JSON.toJson(b));
			} catch (IOException e) {
			}
		}
	}
	
	public void addAddress(String address) {
		if (!address.equals(THIS_SERVER)) {
			synchronized(nodeList) {
				if (!nodeList.contains(address)) {
					nodeList.add(address);
				}
			}
		}
	}

	public void addAddresses(List<String> nodes) {
		if (nodes != null) {
			for (String address : nodes) {
				addAddress(address);
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

	
	public RemoteNodeBCandTP getBestBlockchain() {
		if (!nodeList.isEmpty()) {
			int highestIndex = 0;
			String bestAddress = null;
			for (String address : nodeList) {
				Block b = (Block) JSON.fromJson(HttpService.get(address+"/block/" + Ledger1.GET_LAST_BLOCK),Block.class);
				if ( b.getIndex() > highestIndex) {
					highestIndex = b.getIndex();
					bestAddress = address;
				}
			}
			if (bestAddress != null) {
				String ledger = HttpService.get(bestAddress +"/ledger");
				List<Block> blockchain = JSON.fromJsonToList(ledger, new TypeToken<List<Block>>(){}.getType());
				String tp = HttpService.get(bestAddress +"/pool");
				List<Transaction> transactionPool = JSON.fromJsonToList(tp, new TypeToken<List<Transaction>>(){}.getType());
				return RemoteNodeBCandTP.valueOf(blockchain, transactionPool);
			}
		}
		return null;
	}

}
