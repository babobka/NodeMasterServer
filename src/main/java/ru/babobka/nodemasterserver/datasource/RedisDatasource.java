package ru.babobka.nodemasterserver.datasource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class RedisDatasource {

	private static final String HOST = "localhost";

	private static final int PORT = 6379;

	private static final int DATABASE_NUMBER = 1;

	private static volatile RedisDatasource instance;

	private JedisPool pool;

	private RedisDatasource() {
		pool = new JedisPool(new GenericObjectPoolConfig(), HOST, PORT,
				Protocol.DEFAULT_TIMEOUT, null, DATABASE_NUMBER, null);

	}

	public static RedisDatasource getInstance() {
		RedisDatasource localInstance = instance;
		if (localInstance == null) {
			synchronized (RedisDatasource.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new RedisDatasource();
				}
			}
		}
		return localInstance;
	}

	public JedisPool getPool() {
		return pool;
	}

}
