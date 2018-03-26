package com.block.service;

import com.block.commons.RequestUtil;

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
//		broadcastService.addAddress(RequestUtil.getRequestOriginatingUrl(request));
		return broadcastService.getAddresses();
	}


}
