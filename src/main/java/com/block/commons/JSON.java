package com.block.commons;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSON {

	private JSON() {
	}

	public static String toJson(Object o) {
		Gson mapper = new Gson();
		return mapper.toJson(o);
	}

	public static <T> Object fromJson(String json, Class<T> classOfT) {
		Gson mapper = new GsonBuilder().setPrettyPrinting().create();
		return mapper.fromJson(json, classOfT);
	}

	public static <T> List<T> fromJsonToList(String json, Type type) {
		Gson mapper = new GsonBuilder().setPrettyPrinting().create();
		return mapper.fromJson(json, type);
	}

}
