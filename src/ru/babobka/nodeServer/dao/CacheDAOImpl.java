package ru.babobka.nodeServer.dao;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.datasource.RedisDatasource;

public class CacheDAOImpl implements CacheDAO {

	private static volatile CacheDAOImpl instance;

	private static final String NODE_RESPONSES = "node:responses:";

	private static final int MONTH_SECONDS = 60 * 60 * 24 * 30;

	private CacheDAOImpl() {

	}

	public static CacheDAOImpl getInstance() {
		CacheDAOImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (CacheDAOImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new CacheDAOImpl();
				}
			}
		}
		return localInstance;
	}

	@Override
	public String get(String key) {
		Jedis jedis = null;
		try {
			jedis = RedisDatasource.getInstance().getPool().getResource();
			return jedis.hget(NODE_RESPONSES, key);
		} catch (Exception e) {
			Server.getLogger().log(e);
		} finally {
			if (jedis != null)
				jedis.close();
		}
		return null;
	}

	@Override
	public boolean put(String key, byte[] value) {
		Jedis jedis = null;
		Transaction t = null;
		try {
			byte[] keyBytes = key.getBytes();
			jedis = RedisDatasource.getInstance().getPool().getResource();
			t = jedis.multi();
			t.hset(NODE_RESPONSES.getBytes(), keyBytes, value);
			t.expire(keyBytes, MONTH_SECONDS);
			t.exec();
			return true;
		} catch (Exception e) {
			Server.getLogger().log(e);
			if (t != null) {
				t.discard();
			}
		} finally {
			if (jedis != null)
				jedis.close();
		}
		return false;
	}

}
