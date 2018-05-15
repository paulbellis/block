package com.block.message;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.block.commons.JSON;

public class TestBroadcastNodesMessage {

	@Test
	public void testJson() {
		Collection<String> nodesList = new ArrayList<String>();
		nodesList.add("node1");
		nodesList.add("node2");
		BroadcastNodesMessage bnm = new BroadcastNodesMessage("abc", nodesList );
		assertTrue(JSON.toJson(bnm).equals("{\"sentFrom\":\"abc\",\"nodes\":[\"node1\",\"node2\"]}"));
	}
}
