package com.block.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.block.commons.JSON;
import com.block.model.Block;
import com.block.model.ResultSet;

@RunWith(MockitoJUnitRunner.class)
public class TestBlockService {

	@Test
	public void testGetBlockLedgerNullCaught() {
		ResultSet rs = (ResultSet) JSON.fromJson((String) BlockService.getBlock(null, null, null), ResultSet.class);
		assertTrue(rs.getStatus().equals(ResultSet.ERROR));
		Object o = JSON.fromJson((String)rs.getData(), Object.class);
		assertTrue(o.equals("Null Pointer"));
	}

	@Test
	public void testGetBlockNullHashNullIndex() {
		LedgerService ledger = Mockito.mock(LedgerService.class);
		ResultSet rs = (ResultSet) JSON.fromJson((String) BlockService.getBlock(null, null, ledger), ResultSet.class);
		assertTrue(rs.getStatus().equals(ResultSet.ERROR));
		Object o = JSON.fromJson((String)rs.getData(), Object.class);
		assertTrue(o.equals("Invalid Argument"));
	}

	@Test
	public void testGetBlockNullHashInvalidIndex() {
		ResultSet rs = (ResultSet) JSON.fromJson((String) BlockService.getBlock(null, "abc", null), ResultSet.class);
		assertTrue(rs.getStatus().equals(ResultSet.ERROR));
		Object o = JSON.fromJson((String)rs.getData(), Object.class);
		assertTrue(o.equals("Null Pointer"));
	}

	@Test
	public void testGetBlockWithHash() {
		LedgerService ledger = Mockito.mock(LedgerService.class);
		Block b = Block.createGenesisBlock();
		Mockito.when(ledger.getBlock(Mockito.anyString())).thenReturn(b);
		ResultSet rs = (ResultSet) JSON.fromJson((String) BlockService.getBlock("abc", "123", ledger), ResultSet.class);
		assertTrue(rs.getStatus().equals(ResultSet.SUCCESS));
	}

}
