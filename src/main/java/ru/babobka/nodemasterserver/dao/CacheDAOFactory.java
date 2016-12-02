package ru.babobka.nodemasterserver.dao;

public interface CacheDAOFactory {

	public static CacheDAO get(boolean debug) {
		if (debug) {
			return DebugCacheDAOImpl.getInstance();
		} else {
			return CacheDAOImpl.getInstance();
		}
	}

}
