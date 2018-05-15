package com.block.message;

import com.block.commons.JSON;
import com.block.model.MessageParameters;
import com.block.model.ResultSet;
import com.block.service.BroadcastService;
import com.block.service.Ledgers;
import com.block.service.P2PService;

import spark.Request;
import spark.Response;
import spark.Route;

public class ProcessMessage implements Route {

	private Ledgers ledger;
	private BroadcastService broadcastService;

	public ProcessMessage(MessageParameters params) {
		super();
		this.ledger = params.getLedgerService();
		this.broadcastService = params.getBroadcastService();
	}


	
	@Override
	public Object handle(Request request, Response response) throws Exception {
		Message msg = (Message) JSON.fromJson(request.body(), Message.class);
		if (msg != null) {
			MessageHeader header = msg.getHeader();
			MessageBody body = msg.getBody();
			switch (header.getType()) {
			case BROADCAST_NODES:
				BroadcastNodesMessage bnm = (BroadcastNodesMessage) JSON.fromJson((String)body.getBody(), BroadcastNodesMessage.class);
				return P2PService.processBroadcastNodesMessage(broadcastService, bnm);
			case SEED_NODE:
				BroadcastNodesMessage seed = (BroadcastNodesMessage) JSON.fromJson((String)body.getBody(), BroadcastNodesMessage.class);
				if (!broadcastService.getAddressList().containsAll(seed.getNodes())) {
					broadcastService.addAddresses(seed.getNodes());
					broadcastService.getAndProcessBestBlockChain(ledger);
					broadcastService.broadCastMe();
				}
				return new ResultSet.ResultSetBuilder().setOkStatus().build();
			case SERVER_NODES:
				return new ResultSet.ResultSetBuilder().setOkStatus().setData(broadcastService.getAddresses()).build();
			case GET_LAST_BLOCK_STATS:
				return new ResultSet.ResultSetBuilder().setOkStatus().setData(ledger.getStats(null)).build();
			default:
				return new ResultSet.ResultSetBuilder().setErrorStatus().setData("Unknown message type").build();
			}
		}
		return null;
	}

}
