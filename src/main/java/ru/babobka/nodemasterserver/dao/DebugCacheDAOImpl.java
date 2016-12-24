package ru.babobka.nodemasterserver.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DebugCacheDAOImpl implements CacheDAO {

	private final Map<String, String> debugDataMap = new ConcurrentHashMap<>();


	@Override
	public String get(String key) {
		return debugDataMap.get(key);
	}

	@Override
	public boolean put(String key, String value) {
		debugDataMap.put(key, value);
		return true;
	}

}
