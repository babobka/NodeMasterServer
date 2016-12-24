package ru.babobka.nodemasterserver.datasource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class RedisDatasource {

	private static final String HOST = "localhost";

	private static final int PORT = 6379;

	public enum DatabaseNumber {
		PRODUCTION_DATABASE(1), TEST_DATABASE(2);

		private final int number;

		private DatabaseNumber(int number) {
			this.number = number;
		}

		public int getNumber() {
			return number;
		}
	};

	private JedisPool pool;

	public RedisDatasource(DatabaseNumber databaseNumber) {
		pool = new JedisPool(new GenericObjectPoolConfig(), HOST, PORT,
				Protocol.DEFAULT_TIMEOUT, null, databaseNumber.getNumber(),
				null);

	}

	public JedisPool getPool() {
		return pool;
	}

}
