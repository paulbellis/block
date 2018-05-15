package com.block.model;

import com.block.service.BroadcastService;
import com.block.service.Ledgers;

public class MessageParameters {
	
	private Parameters p;
	private static String BROADCAST_SERVICE = "BROADCAST_SERVICE";
	private static String LEDGER_SERVICE = "LEDGER_SERVICE";
	public MessageParameters() {
		p = new Parameters();
	}
	
	public void setBroadcastService(BroadcastService value) {
		if (value!=null) {
			p.setParams(BROADCAST_SERVICE, value);
		}
	}
	
	public BroadcastService getBroadcastService() {
		return (BroadcastService) p.getParam(BROADCAST_SERVICE);
	}

	
	public void setLedgerService(Ledgers value) {
		if (value != null) {
			p.setParams(LEDGER_SERVICE, value);
		}
	}
	
	public Ledgers getLedgerService() {
		return (Ledgers) p.getParam(LEDGER_SERVICE);
	}

}
