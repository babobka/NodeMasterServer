package ru.babobka.nodemasterserver.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.server.ServerContext;

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
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource();) {
			return jedis.hget(NODE_RESPONSES, key);
		} catch (Exception e) {
			ServerContext.getInstance().getLogger().log(e);
		}
		return null;
	}

	@Override
	public boolean put(String key, byte[] value) {
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource(); Transaction t = jedis.multi()) {
			byte[] keyBytes = key.getBytes();
			t.hset(NODE_RESPONSES.getBytes(), keyBytes, value);
			t.expire(keyBytes, MONTH_SECONDS);
			t.exec();
			return true;
		} catch (Exception e) {
			ServerContext.getInstance().getLogger().log(e);
		}
		return false;
	}

}
