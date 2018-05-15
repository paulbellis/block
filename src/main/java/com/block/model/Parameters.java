package com.block.model;

import java.util.HashMap;
import java.util.Map;

public class Parameters {
	Map<String,Object> param = new HashMap<>();

	public Object getParam(String key) {
		return param.get(key);
	}

	public void setParams(String key, Object value) {
		param.put(key,value);
	}
	
	
}
