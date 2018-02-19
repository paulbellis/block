package com.block.rest;

import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.stop;

import com.block.model.Ledger1;
import com.block.service.Create;
import com.block.service.DummyStore;
import com.block.service.Dump;
import com.block.service.DumpLedger;
import com.block.service.Ledger;
import com.block.service.TransferService;

public class Server {

	private static DummyStore db = new DummyStore();
	private static Ledger1 ledger = new Ledger1(null);
	private static TransferService transferService = new TransferService(db, ledger);
	

	public static void start() {
		get("/ledger", new DumpLedger(ledger));
		get("/dump", new Dump(db));
		post("/create", new Create(db,ledger));
		put("/transfer", new Transfer(transferService));
	}

	public static void stopServer() {
		stop();
	}

	public static void main(String[] args) {
		Server.start();
	}

}
