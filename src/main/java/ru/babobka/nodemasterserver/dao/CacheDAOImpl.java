package ru.babobka.nodemasterserver.dao;

import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.server.MasterServerContext;

class CacheDAOImpl implements CacheDAO {

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
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource()) {
			return jedis.hget(NODE_RESPONSES, key);
		} catch (Exception e) {
			MasterServerContext.getInstance().getLogger().log(e);
		}
		return null;
	}

	@Override
	public boolean put(String key, String value) {
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource(); Transaction t = jedis.multi()) {	
			t.hset(NODE_RESPONSES, key, value);
			t.expire(key, MONTH_SECONDS);
			t.exec();
			return true;
		} catch (IOException e) {
			MasterServerContext.getInstance().getLogger().log(e);
		}
		return false;
	}

}
