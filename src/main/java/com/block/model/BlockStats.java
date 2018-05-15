package com.block.model;

import java.util.HashMap;
import java.util.Map;

public class BlockStats {
	
	private Map<String,Object> stats;
	public static String CURRENT_LAST_HEADER = "current_last_header";
	public static String CUMMULATIVE_DIFFICULTY = "cummulative_difficulty";
	
	public BlockStats() {
		stats = new HashMap<>();
	}
	public Map<String,Object> getStats() {
		return stats;
	}

	public void setStats(Map<String,Object> stats) {
		this.stats = stats;
	}

	public void setStat(String key, Object value) {
		stats.put(key, value);
	}
	
	public Object getStat(String key) {
		return stats.get(key);
	}

	public static BlockStats create() {
		return new BlockStats();
	}
}
