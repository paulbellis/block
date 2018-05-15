package com.block.service;

import java.util.List;
import java.util.stream.Collectors;

import com.block.message.BroadcastNodesMessage;
import com.block.model.ResultSet;

public class P2PService {

	public static String processBroadcastNodesMessage(BroadcastService broadcastService, BroadcastNodesMessage bnm) {
		if (broadcastService == null || bnm == null) {
			return new ResultSet.ResultSetBuilder().setErrorStatus().setData("Null pointer exception").build();
		}
		List<String> addressesExcludingSender = broadcastService.getAddressList().stream()
				.filter((String a) -> !a.equals(bnm.getSentFrom())).collect(Collectors.toList());
		addressesExcludingSender.removeAll(bnm.getNodes());
		broadcastService.addAddresses(bnm.getNodes());
		broadcastService.broadCastMeTo(addressesExcludingSender, broadcastService.getAddressList());
		broadcastService.addAddress(bnm.getSentFrom());
		return new ResultSet.ResultSetBuilder().setOkStatus().build();
	}
}
