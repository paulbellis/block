package com.block.rest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.block.commons.JSON;
import com.block.model.AccountTransfer;
import com.block.service.Ledgers;
import com.block.service.TransferService;

import spark.Request;
import spark.Response;
import spark.Route;

public class TransferManager implements Route {

	private static Logger log = LogManager.getLogger(TransferManager.class);
	private Ledgers ledger;

	public TransferManager(Ledgers ledger) {
		super();
		this.ledger = ledger;
	}

	public Object handle(Request request, Response response) throws Exception {
		AccountTransfer transfer = (AccountTransfer) JSON.fromJson(request.body(), AccountTransfer.class);
		return TransferService.transfer(ledger, transfer);
	}

}
