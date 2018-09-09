package com.block.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.block.rest.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.block.commons.JSON;
import com.block.commons.RemoteNodeBCandTP;
import com.block.commons.URLBuilder;
import com.block.message.BroadcastNodesMessage;
import com.block.message.Message;
import com.block.message.MessageBody;
import com.block.message.MessageHeader;
import com.block.message.MessageType;
import com.block.model.Block;
import com.block.model.BlockStats;
import com.block.model.ResultSet;
import com.block.model.Transaction;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

public class BroadcastService {

	private static Logger log = LogManager.getLogger(LedgerService.class);
	private Set<String> nodeList = new ConcurrentSkipListSet<>();
	private static volatile BroadcastService instance = null;
	private String thisNodeUrl;
	private int thisNodePort;
	public static String THIS_SERVER = null;

	public BroadcastService(String url, int port) {
		thisNodeUrl = url;
		thisNodePort = port;
		THIS_SERVER = thisNodeUrl + ":" + thisNodePort;
	}

	public void broadcastTransaction(Transaction t) {
		for (String address : nodeList) {
			try {
				HttpService.post(address + Api.API_TRANSACTION, JSON.toJson(t));
			} catch (IOException e) {
			}
		}
	}

	public void broadcastBlock(Block b) {
		for (String address : nodeList) {
			try {
				String url = address + Api.API_BLOCK +"?server=";
				HttpService.post(url + URLEncoder.encode(THIS_SERVER, StandardCharsets.UTF_8.toString()),
						JSON.toJson(b));
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}

	public void addAddress(String address) {
		if (!address.equals(THIS_SERVER)) {
			synchronized (nodeList) {
				if (!nodeList.contains(address)) {
					nodeList.add(address);
				}
			}
		}
	}

	public void removeAddress(String address) {
		synchronized (nodeList) {
			nodeList.remove(address);
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

	public Collection<String> getAddressList() {
		return nodeList;
	}

	public void getNetworkNodes() {
		for (String address : nodeList) {
			List<String> newNodes = JSON.fromJsonToList(HttpService.get(address + Api.API_SERVERS),
					new TypeToken<List<String>>() {
					}.getType());
			addAddresses(newNodes);
		}
	}

	public void broadCastMe() {
		for (String address : broadCastMeTo(nodeList, nodeList)) {
			removeAddress(address);
		}
	}

	public List<String> broadCastMeTo(Collection<String> addressList, Collection<String> nodeList) {
		List<String> failedAddresses = new ArrayList<>();
		for (String address : addressList) {
			try {
				HttpService.post(address + Api.API_SERVERS, JSON.toJson(Message.create(
						MessageHeader.create(MessageType.BROADCAST_NODES),
						MessageBody.create(JSON.toJson(BroadcastNodesMessage.createMessage(THIS_SERVER, nodeList))))));
			} catch (IOException e) {
				log.error(e.getMessage());
				failedAddresses.add(address);
			}
			
		}
		return failedAddresses;
	}

	
	public RemoteNodeBCandTP getBestBlockchain() {
		if (!nodeList.isEmpty()) {
			int highestIndex = 0;
			double highestCumulativeDifficulty = 0;
			String bestAddress = null;
			for (String address : nodeList) {
//				ResultSet rs = (ResultSet) JSON
//						.fromJson(HttpService.get(address + "/block/" + LedgerService.GET_LAST_BLOCK), ResultSet.class);
				ResultSet rs;
				try {
					rs = (ResultSet) JSON.fromJson(HttpService.post(URLBuilder.stats(address), "{\"header\":{\"type\":\"GET_LAST_BLOCK_STATS\"},\"body\":null}"), ResultSet.class);
					Map<String, Object> x = JSON.fromJsonToMap((String)rs.getData(), new TypeToken<Map<String, Object>>(){}.getType());
					BlockStats o = (BlockStats) JSON.fromJson((String)rs.getData(),BlockStats.class);
					//Object s1 = (((LinkedTreeMap)o).get("stats"));
					
					LinkedTreeMap bs = (LinkedTreeMap) o.getStat(BlockStats.CURRENT_LAST_HEADER);
					double blockIndex = (double) bs.get("index");
					System.out.println();
					double cumulativeDifficulty = (double) o.getStat(BlockStats.CUMMULATIVE_DIFFICULTY);
					//Block b = (Block) JSON.fromJson((String) rs.getData(), Block.class);
					if (blockIndex > 0 && cumulativeDifficulty > highestCumulativeDifficulty) {
						highestCumulativeDifficulty = cumulativeDifficulty;
						bestAddress = address;
					}
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			if (bestAddress != null) {
				ResultSet rs = (ResultSet) JSON.fromJson(HttpService.get(bestAddress + Api.API_LEDGER), ResultSet.class);
				String ledger = (String) rs.getData();
				List<Block> blockchain = JSON.fromJsonToList(ledger, new TypeToken<List<Block>>() {
				}.getType());
				String tp = HttpService.get(bestAddress + Api.API_POOL);
				List<Transaction> transactionPool = JSON.fromJsonToList(tp, new TypeToken<List<Transaction>>() {
				}.getType());
				return RemoteNodeBCandTP.valueOf(blockchain, transactionPool);
			}
		}
		return null;
	}
	
	public void getAndProcessBestBlockChain(Ledgers ledger) {
		RemoteNodeBCandTP r = getBestBlockchain();
		if (r != null && r.getBlockChain() != null && !r.getBlockChain().isEmpty()) {
			ledger.processNewBlockChain(r.getBlockChain());
		}
		if (r != null && r.getTransactionPool() != null && !r.getTransactionPool().isEmpty()) {
			ledger.processNewTransactionPool(r.getTransactionPool());
		}

	}
	public void peerPing(Ledgers ledger) {
		getNetworkNodes();
		broadCastMe();
		getAndProcessBestBlockChain(ledger);
	}
}
