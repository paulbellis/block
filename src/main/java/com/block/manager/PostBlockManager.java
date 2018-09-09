package com.block.manager;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.block.commons.JSON;
import com.block.model.Block;
import com.block.service.BlockService;
import com.block.service.Ledgers;

import spark.Request;
import spark.Response;
import spark.Route;

public class PostBlockManager implements Route {

	private Ledgers ledger;

	public PostBlockManager(Ledgers ledger) {
		super();
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		try {
			String originatingServer = URLDecoder.decode(request.queryParams("server"),StandardCharsets.UTF_8.toString());
			Block b = (Block) JSON.fromJson(request.body(), Block.class);
			if (!BlockService.processNewBlock(b, ledger)) {
				return "FAILED TO PROCESS BLOCK";
			}
		} catch (Exception e) {

		}

		return "SUCCESS";
	}

}
