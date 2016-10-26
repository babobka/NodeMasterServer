package ru.babobka.nodemasterserver.server;

import ru.babobka.nodemasterserver.builder.JSONFileServerConfigBuilder;
import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.model.ResponseStorage;
import ru.babobka.vsjws.logger.SimpleLogger;

public class ServerContext {

	private final ServerConfig config;

	private final SimpleLogger logger;

	private final Slaves slaves;

	private final ResponseStorage responseStorage;

	private final int databaseNumber;

	private static volatile ServerContext instance;

	private static volatile boolean production;

	private ServerContext() {
		try {
			config = JSONFileServerConfigBuilder.build(production);
			if (production) {
				databaseNumber = RedisDatasource.PRODUCTION_DATABASE_NUMBER;
			} else {
				databaseNumber = RedisDatasource.TEST_DATABASE_NUMBER;
			}
			logger = new SimpleLogger("NodeServer", config.getLoggerFolder(), "server");
			responseStorage = new ResponseStorage();
			slaves = new Slaves(config.getMaxClients());
			logger.log("ServerContext was successfuly created");
			logger.log(config.toString());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static ServerContext getInstance() {
		ServerContext localInstance = instance;
		if (localInstance == null) {
			synchronized (ServerContext.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new ServerContext();

				}
			}
		}
		return localInstance;
	}

	public ServerConfig getConfig() {
		return config;

	}

	public SimpleLogger getLogger() {
		return logger;
	}

	public Slaves getSlaves() {
		return slaves;
	}

	public ResponseStorage getResponseStorage() {
		return responseStorage;
	}

	static void setProduction(boolean production) {
		if (instance == null) {
			synchronized (ServerContext.class) {
				if (instance == null) {
					ServerContext.production = production;
				}
			}
		}
	}

	public int getDatabaseNumber() {
		return databaseNumber;
	}

}
