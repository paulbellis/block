package com.block.commons;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

public class TestJSON {

	@Test
	public void testToJson() {
		assertTrue(JSON.toJson("hello").equals("\"hello\""));
	}

	@Test
	public void testFromJson() {
		assertTrue(JSON.fromJson("hello", String.class).equals("hello"));
	}

	@Test
	public void testFromJsonToList() {
		List<String> l = JSON.fromJsonToList("[\"a\",\"b\"]", new TypeToken<List<String>>() {}.getType());
		assertTrue(l.size()==2);
	}

	@Test
	public void testFromJsonToMap() {
		Map<String,String> l = JSON.fromJsonToMap("{\"a\":\"b\"}", new TypeToken<Map<String,String>>() {}.getType());
		assertTrue(l.size()==1);
	}

}
