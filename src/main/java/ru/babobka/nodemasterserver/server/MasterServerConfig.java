package ru.babobka.nodemasterserver.server;

import java.io.File;

import org.json.JSONObject;
import ru.babobka.nodemasterserver.exception.ServerConfigurationException;

public class MasterServerConfig {

	private final int maxSlaves;

	private final int authTimeOutMillis;

	private final int mainServerPort;

	private final int requestTimeOutMillis;

	private final int heartBeatTimeOutMillis;

	private final int webPort;

	private final String restServiceLogin;

	private final String restServicePassword;

	private final String loggerFolder;

	private final String tasksFolder;

	private final boolean debugDataBase;

	private final boolean productionDataBase;

	private static final int PORT_MIN = 1024;

	private static final int PORT_MAX = 65535;

	public MasterServerConfig(int maxSlaves, int authTimeOutMillis, int mainServerPort, int requestTimeOutMillis,
			int heartBeatTimeOutMillis, int webPort, String restServiceLogin, String restServicePassword,
			String loggerFolder, String tasksFolder, boolean debugDataBase, boolean productionDataBase) {
		if (maxSlaves <= 0) {
			throw new ServerConfigurationException("'maxSlaves' value must be positive");
		}
		this.maxSlaves = maxSlaves;
		if (authTimeOutMillis <= 0) {
			throw new ServerConfigurationException("'authTimeOutMillis' value must be positive");
		}
		this.authTimeOutMillis = authTimeOutMillis;

		if (mainServerPort <= 0) {
			throw new ServerConfigurationException("'mainServerPort' value must be positive");
		} else if (mainServerPort < PORT_MIN || mainServerPort > PORT_MAX) {
			throw new ServerConfigurationException(
					"'mainServerPort' must be in range [" + PORT_MIN + ";" + PORT_MAX + "]");
		}
		this.mainServerPort = mainServerPort;
		if (requestTimeOutMillis <= 0) {
			throw new ServerConfigurationException("'requestTimeOutMillis' value must be positive");
		}
		this.requestTimeOutMillis = requestTimeOutMillis;
		if (heartBeatTimeOutMillis <= 0) {
			throw new ServerConfigurationException("'heartBeatTimeOutMillis' value must be positive");
		} else if (heartBeatTimeOutMillis >= requestTimeOutMillis) {
			throw new ServerConfigurationException(
					"'heartBeatTimeOutMillis' value must lower than 'requestTimeOutMillis'");
		}
		this.heartBeatTimeOutMillis = heartBeatTimeOutMillis;
		if (webPort <= 0) {
			throw new ServerConfigurationException("'webPort' value must be positive");
		} else if (webPort < PORT_MIN || webPort > PORT_MAX) {
			throw new ServerConfigurationException("'webPort' must be in range [" + PORT_MIN + ";" + PORT_MAX + "]");
		} else if (webPort == mainServerPort) {
			throw new ServerConfigurationException("'webPort' and 'mainServerPort' must not be equal");
		}
		this.webPort = webPort;

		if (restServiceLogin == null) {
			throw new ServerConfigurationException("'restServiceLogin' must not be null");
		}
		this.restServiceLogin = restServiceLogin;
		if (restServicePassword == null) {
			throw new ServerConfigurationException("'restServicePassword' must not be null");
		}
		this.restServicePassword = restServicePassword;

		if (loggerFolder == null) {
			throw new ServerConfigurationException("'loggerFolder' must not be null");
		} else {
			File loggerFile = new File(loggerFolder);
			if (!loggerFile.exists()) {
				loggerFile.mkdirs();
			}
		}

		this.loggerFolder = loggerFolder;

		if (tasksFolder == null) {
			throw new ServerConfigurationException("'tasksFolder' must not be null");
		} else if (!new File(tasksFolder).exists()) {
			throw new ServerConfigurationException("'tasksFolder' " + tasksFolder + " doesn't exist");
		}

		this.tasksFolder = tasksFolder;
		this.productionDataBase = productionDataBase;
		this.debugDataBase = debugDataBase;

	}

	public MasterServerConfig(JSONObject jsonObject) {

		this(jsonObject.getInt("maxSlaves"), jsonObject.getInt("authTimeOutMillis"),
				jsonObject.getInt("mainServerPort"), jsonObject.getInt("requestTimeOutMillis"),
				jsonObject.getInt("heartBeatTimeOutMillis"), jsonObject.getInt("webPort"),
				jsonObject.getString("restServiceLogin"), jsonObject.getString("restServicePassword"),
				jsonObject.getString("loggerFolder"), jsonObject.getString("tasksFolder"),
				jsonObject.getBoolean("debugDataBase"), jsonObject.getBoolean("productionDataBase"));
	}

	public int getAuthTimeOutMillis() {
		return authTimeOutMillis;
	}

	public int getMainServerPort() {
		return mainServerPort;
	}

	public int getRequestTimeOutMillis() {
		return requestTimeOutMillis;
	}

	public int getHeartBeatTimeOutMillis() {
		return heartBeatTimeOutMillis;
	}

	public int getWebPort() {
		return webPort;
	}

	public int getMaxSlaves() {
		return maxSlaves;
	}

	public String getRestServiceLogin() {
		return restServiceLogin;
	}

	public String getRestServicePassword() {
		return restServicePassword;
	}

	public String getLoggerFolder() {
		return loggerFolder;
	}

	public String getTasksFolder() {
		return tasksFolder;
	}

	public boolean isDebugDataBase() {
		return debugDataBase;
	}

	public boolean isProductionDataBase() {
		return productionDataBase;
	}

	@Override
	public String toString() {
		return "MasterServerConfig [maxSlaves=" + maxSlaves + ", authTimeOutMillis=" + authTimeOutMillis
				+ ", mainServerPort=" + mainServerPort + ", requestTimeOutMillis=" + requestTimeOutMillis
				+ ", heartBeatTimeOutMillis=" + heartBeatTimeOutMillis + ", webPort=" + webPort + ", restServiceLogin="
				+ restServiceLogin + ", restServicePassword=" + restServicePassword + ", loggerFolder=" + loggerFolder
				+ ", tasksFolder=" + tasksFolder + ", debugDataBase=" + debugDataBase + ", productionDataBase="
				+ productionDataBase + "]";
	}

}
