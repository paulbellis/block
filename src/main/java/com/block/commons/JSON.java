package com.block.commons;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class JSON {

	private static Logger log = LogManager.getLogger(JSON.class);
	private JSON() {
	}

	public static String toJson(Object o) {
		Gson mapper = new Gson();
		return mapper.toJson(o);
	}

	public static <T> Object fromJson(String json, Class<T> classOfT) {
		Gson mapper = new GsonBuilder().setPrettyPrinting().create();
		Object o = null;
		try {
			o = mapper.fromJson(json, classOfT); 
		}
		catch (JsonParseException e) {
			log.error(e.getMessage());
		}
		return o;
	}

	public static <T> List<T> fromJsonToList(String json, Type type) {
		Gson mapper = new GsonBuilder().setPrettyPrinting().create();
		List<T> l = null;
		try {
			l = mapper.fromJson(json, type); 
		}
		catch (JsonParseException e) {
			log.error(e.getMessage());
		}
		return l;
	}

//	Type type = new TypeToken<Map<String, String>>(){}.getType();
//	Map<String, String> myMap = gson.fromJson("{'k1':'apple','k2':'orange'}", type);
	public static <K,V> Map<K,V> fromJsonToMap(String json, Type type) {
		Gson mapper = new GsonBuilder().setPrettyPrinting().create();
		Map<K,V> m = null;
		try {
			m = mapper.fromJson(json, type); 
		}
		catch (JsonParseException e) {
			log.error(e.getMessage());
		}
		return m;
	}

	
}
