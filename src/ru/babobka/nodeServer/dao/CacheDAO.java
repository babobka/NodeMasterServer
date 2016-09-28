package ru.babobka.nodeServer.dao;

public interface CacheDAO {

	String get(String key);

	boolean put(String key, byte[] value);
	

}
