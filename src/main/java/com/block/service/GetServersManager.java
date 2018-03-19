package com.block.service;

import spark.Request;
import spark.Response;
import spark.Route;

public class GetServersManager implements Route {

	private BroadcastService broadcastService;

	public GetServersManager(BroadcastService broadcastService) {
		super();
		this.broadcastService = broadcastService;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return broadcastService.getAddresses();
	}


}
