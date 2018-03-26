package com.block.manager;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.block.commons.JSON;
import com.block.model.Block;
import com.block.model.Ledger1;
import com.block.service.BlockService;

import spark.Request;
import spark.Response;
import spark.Route;

public class PostBlockManager implements Route {

	private Ledger1 ledger;

	public PostBlockManager(Ledger1 ledger) {
		super();
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		try {
			String originatingServer = URLDecoder.decode(request.queryParams("server"),StandardCharsets.UTF_8.toString());
			Block b = (Block) JSON.fromJson(request.body(), Block.class);
			BlockService.processNewBlock(b, ledger, originatingServer);
		} catch (Exception e) {

		}

		return "SUCCESS";
	}

}
