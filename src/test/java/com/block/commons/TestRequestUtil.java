package com.block.commons;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class TestRequestUtil {

	@Test
	public void testGetRequestOriginatingUrl() {
		Request request = mock(Request.class);
		when(request.uri()).thenReturn("/block");
		when(request.url()).thenReturn("http://localhost:4567/block");
		assertTrue(RequestUtil.getRequestOriginatingUrl(request).equals("http://localhost:4567"));
	}

	@Test
	public void testGetRequestOriginatingUrlRoot() {
		Request request = mock(Request.class);
		when(request.uri()).thenReturn("/");
		when(request.url()).thenReturn("http://localhost:4567/");
		assertTrue(RequestUtil.getRequestOriginatingUrl(request).equals("http://localhost:4567"));
	}

	@Test
	public void testGetRequestOriginatingUrlDoesNotContainUri() {
		Request request = mock(Request.class);
		when(request.uri()).thenReturn("xxx");
		when(request.url()).thenReturn("http://localhost:4567/");
		assertTrue(RequestUtil.getRequestOriginatingUrl(request).equals(""));
	}

}
