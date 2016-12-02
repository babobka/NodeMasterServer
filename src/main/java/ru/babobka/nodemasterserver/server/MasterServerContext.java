package ru.babobka.nodemasterserver.server;

import java.io.File;
import java.util.logging.Level;

import ru.babobka.nodemasterserver.builder.JSONFileServerConfigBuilder;
import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.model.ResponseStorage;

public class MasterServerContext {

	private static String configPath;

	private final MasterServerConfig config;

	private final SimpleLogger logger;

	private final Slaves slaves;

	private final ResponseStorage responseStorage;

	private final int databaseNumber;

	private static volatile MasterServerContext instance;

	private MasterServerContext() {
		try {
			if (configPath == null) {
				throw new IllegalStateException("'configPath' was not specified.");
			}
			config = JSONFileServerConfigBuilder.build(configPath);
			if (config.isProductionDataBase()) {
				databaseNumber = RedisDatasource.PRODUCTION_DATABASE_NUMBER;
			} else {
				databaseNumber = RedisDatasource.TEST_DATABASE_NUMBER;
			}
			logger = new SimpleLogger("NodeServer", config.getLoggerFolder(), "server");
			responseStorage = new ResponseStorage();
			slaves = new Slaves(config.getMaxSlaves());
			logger.log("ServerContext was successfuly created");
			logger.log(config.toString());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static MasterServerContext getInstance() {
		MasterServerContext localInstance = instance;
		if (localInstance == null) {
			synchronized (MasterServerContext.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new MasterServerContext();

				}
			}
		}
		return localInstance;
	}

	public MasterServerConfig getConfig() {
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

	public int getDatabaseNumber() {
		return databaseNumber;
	}

	public static synchronized String getConfigPath() {
		return configPath;
	}

	public static synchronized void setConfigPath(String configPath) {
		if (instance == null) {
			File f = new File(configPath);
			if (f.exists() && !f.isDirectory()) {
				MasterServerContext.configPath = configPath;
			} else {
				throw new RuntimeException("'configPath' " + configPath + " doesn't exists");
			}

		} else {
			instance.logger.log(Level.WARNING, "Can not define 'configFolder' value. Context is already created.");
		}
	}

}
