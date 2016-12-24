package ru.babobka.nodemasterserver.dao;

import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.logger.SimpleLogger;

public class CacheDAOImpl implements CacheDAO {

	private static final String NODE_RESPONSES = "node:responses:";

	private static final int MONTH_SECONDS = 60 * 60 * 24 * 30;

	private final RedisDatasource datasource = Container.getInstance()
			.get(RedisDatasource.class);

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	@Override
	public String get(String key) {
		try (Jedis jedis = datasource.getPool().getResource()) {
			return jedis.hget(NODE_RESPONSES, key);
		} catch (Exception e) {
			logger.log(e);
		}
		return null;
	}

	@Override
	public boolean put(String key, String value) {
		try (Jedis jedis = datasource.getPool().getResource();
				Transaction t = jedis.multi()) {
			t.hset(NODE_RESPONSES, key, value);
			t.expire(key, MONTH_SECONDS);
			t.exec();
			return true;
		} catch (IOException e) {
			logger.log(e);
		}
		return false;
	}

}
