package ru.babobka.nodemasterserver.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugCacheDAOImpl implements CacheDAO {

	private static volatile DebugCacheDAOImpl instance;

	private final Map<String, String> debugDataMap = new ConcurrentHashMap<>();

	private DebugCacheDAOImpl() {

	}

	public static DebugCacheDAOImpl getInstance() {
		DebugCacheDAOImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (DebugCacheDAOImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new DebugCacheDAOImpl();
				}
			}
		}
		return localInstance;
	}

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
