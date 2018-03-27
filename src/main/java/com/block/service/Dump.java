package com.block.service;

import com.block.model.DummyStore;

import spark.Request;
import spark.Response;
import spark.Route;

public class Dump implements Route {

	private DummyStore db;
	public Dump(DummyStore db) {
		this.db = db;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return db.dump();
	}

}
