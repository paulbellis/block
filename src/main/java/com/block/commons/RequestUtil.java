package com.block.commons;

import spark.Request;

public class RequestUtil {

	public static String getRequestOriginatingUrl(Request request) {
		if (request == null) {
			throw new IllegalArgumentException();
		}
		int index = request.url().lastIndexOf(request.uri());
		if (index == -1) {
			return "";
		}
		return request.url().substring(0 , index);
	}
}
