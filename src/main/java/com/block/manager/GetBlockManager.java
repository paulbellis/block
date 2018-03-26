package com.block.manager;

import com.block.commons.JSON;
import com.block.model.Block;
import com.block.model.Ledger1;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetBlockManager implements Route {

	Ledger1 ledger;
	
	public GetBlockManager(Ledger1 ledger) {
		super();
		this.ledger = ledger;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		String hash = request.params("hash");
		Block b = null;
		if (hash != null) {
			b = ledger.getBlock(hash);
		}
		else {
			String i = request.queryParams("index");
			if ( i != null ) {
				try {
					Integer index = Integer.valueOf(i);
					b = ledger.getBlock(index);
				} catch (NumberFormatException  e) {
					return "Invalid number for index request of block";
				}
			}
		}
		return (b == null ? "No such block" : JSON.toJson(b));
	}

}
