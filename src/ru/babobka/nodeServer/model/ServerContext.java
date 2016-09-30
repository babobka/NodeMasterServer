package ru.babobka.nodeServer.model;

import ru.babobka.nodeServer.builder.JSONFileServerConfigBuilder;

import ru.babobka.nodeServer.util.StreamUtil;
import ru.babobka.vsjsw.logger.SimpleLogger;

public class ServerContext {

	private final ServerConfig config;

	private final SimpleLogger logger;

	private final String runningFolder;

	private final ClientThreads clientThreads;

	private final ResponseStorage responseStorage;

	private static volatile ServerContext instance;

	private ServerContext() {
		try {
			runningFolder = StreamUtil.getRunningFolder();
			config = JSONFileServerConfigBuilder.build(runningFolder);
			logger = new SimpleLogger("NodeServer", runningFolder, "server");
			responseStorage = new ResponseStorage();
			clientThreads = new ClientThreads(config.getMaxClients());
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

	public String getRunningFolder() {
		return runningFolder;
	}

	public ClientThreads getClientThreads() {
		return clientThreads;
	}

	public ResponseStorage getResponseStorage() {
		return responseStorage;
	}

}
