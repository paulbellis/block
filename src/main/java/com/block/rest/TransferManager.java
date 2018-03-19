package com.block.rest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import com.block.commons.AccountNotExistException;
import com.block.commons.InsufficientFundsException;
import com.block.commons.JSON;
import com.block.model.AccountTransfer;
import com.block.service.TransferService;

import spark.Request;
import spark.Response;
import spark.Route;

public class TransferManager implements Route {

	private static Logger log = LogManager.getLogger(TransferManager.class);
	private TransferService transferService;

	public TransferManager(TransferService transferService) {
		this.transferService = transferService;
	}

	public Object handle(Request request, Response response) throws Exception {
		AccountTransfer transfer = (AccountTransfer) JSON.fromJson(request.body(), AccountTransfer.class);
		try {
			log.debug("Received " + transfer);
			transferService.transfer(transfer);
		} catch (AccountNotExistException | InsufficientFundsException e) {
			log.error(e.getMessage());
			response.status(HttpStatus.PRECONDITION_FAILED_412);
			return JSON.toJson(transfer);
		}
		log.debug("Completed " + transfer);
		response.status(HttpStatus.OK_200);
		return JSON.toJson(transfer);
	}

}
