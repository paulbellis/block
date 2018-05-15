package com.block.message;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.block.commons.JSON;

public class TestMessage {

	@Test
	public void testCreateServerPostMessage() {
		Collection<String> nodeList = new ArrayList<>();
		nodeList.add("http://localhost:4567");
		System.out.println(JSON.toJson(Message.create(MessageHeader.create(MessageType.BROADCAST_NODES),
				MessageBody.create(JSON.toJson(BroadcastNodesMessage.createMessage("", nodeList))))));
	}

}
