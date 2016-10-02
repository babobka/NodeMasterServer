package ru.babobka.nodemasterserver.dao;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.server.ServerContext;
import ru.babobka.nodemasterserver.util.DateUtil;

public class StatisticsDAOImpl implements StatisticsDAO {

	private static final String STATISTICS_KEY = "statistics:";

	private static final int TWO_MONTHS_SECONDS = 60 * 60 * 24 * 30 * 2;

	private static volatile StatisticsDAOImpl instance;

	private StatisticsDAOImpl() {

	}

	public static StatisticsDAOImpl getInstance() {
		StatisticsDAOImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (StatisticsDAOImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new StatisticsDAOImpl();
				}
			}
		}
		return localInstance;
	}

	@Override
	public void incrementRequests() {
		Jedis jedis = null;
		Transaction t = null;
		try {
			jedis = RedisDatasource.getInstance().getPool().getResource();
			t = jedis.multi();
			String monthDate = DateUtil.getMonthYear();
			int currentHour = DateUtil.getCurrentHour();
			boolean expireable = false;
			String key = STATISTICS_KEY + monthDate;
			if (jedis.exists(key)) {
				expireable = true;
			}
			t.hincrBy(key, String.valueOf(currentHour), 1);
			if (expireable) {
				t.expire(key, TWO_MONTHS_SECONDS);
			}
			t.exec();
		} catch (Exception e) {
			ServerContext.getInstance().getLogger().log(e);
			if (t != null) {
				t.discard();
			}
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

}
