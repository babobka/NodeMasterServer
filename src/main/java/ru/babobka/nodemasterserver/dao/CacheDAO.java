package ru.babobka.nodemasterserver.dao;

public interface CacheDAO {

	String get(String key);

	boolean put(String key, String value);
	

}
