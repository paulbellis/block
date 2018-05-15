package com.block.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BroadcastNodesMessage {

	private String sentFrom;
	private List<String> nodes;

	public BroadcastNodesMessage(String sentFrom, Collection<String> nodeList) {
		super();
		this.sentFrom = sentFrom;
		this.nodes = new ArrayList<>(nodeList);
	}

	public String getSentFrom() {
		return sentFrom;
	}
	
	public List<String> getNodes() {
		return nodes;
	}
	
	public static BroadcastNodesMessage createMessage(String sentFrom, Collection<String> nodeList) {
		return new BroadcastNodesMessage(sentFrom, nodeList);
	}
	
}
