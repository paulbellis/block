package com.block.manager;

import java.util.List;

import com.block.commons.JSON;
import com.block.service.BroadcastService;
import com.google.gson.reflect.TypeToken;

import spark.Request;
import spark.Response;
import spark.Route;

public class PostServersManager implements Route {

	private BroadcastService broadcastService;

	public PostServersManager(BroadcastService broadcastService) {
		super();
		this.broadcastService = broadcastService;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		String body = request.body();
		List<String> addresses = JSON.fromJsonToList(body , new TypeToken<List<String>>(){}.getType()); 
		if (!addresses.isEmpty()) {
			broadcastService.addAddresses(addresses);
		}
		return 0;
	}


}
