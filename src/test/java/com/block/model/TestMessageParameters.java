package com.block.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.block.service.BroadcastService;
import com.block.service.LedgerService;
import com.block.service.Ledgers;

@RunWith(MockitoJUnitRunner.class)
public class TestMessageParameters {

	@Test
	public void testSetBroadcastServiceWithNullPassedIn() {
		MessageParameters p = new MessageParameters();
		p.setBroadcastService(null);
		assertTrue(p.getBroadcastService() == null);
	}

	@Test
	public void testSetBroadcastService() {
		MessageParameters p = new MessageParameters();
		BroadcastService broadcastService = Mockito.mock(BroadcastService.class);
		p.setBroadcastService(broadcastService);
		assertTrue(p.getBroadcastService() == broadcastService);
		assertTrue(p.getBroadcastService() instanceof BroadcastService);
	}

	@Test
	public void testSetLedgerServiceWithNullPassedIn() {
		MessageParameters p = new MessageParameters();
		p.setLedgerService(null);
		assertTrue(p.getLedgerService() == null);
	}

	@Test
	public void testSetLedgerService() {
		MessageParameters p = new MessageParameters();
		Ledgers ledger = Mockito.mock(LedgerService.class);
		p.setLedgerService(ledger);
		assertTrue(p.getLedgerService() == ledger);
		assertTrue(p.getLedgerService() instanceof LedgerService);
	}

}
