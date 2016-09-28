package ru.babobka.nodeServer.model;

import java.util.logging.Level;

import org.json.JSONObject;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.constant.RegularPatterns;
import ru.babobka.nodeServer.exception.ServerConfigurationException;
import ru.babobka.nodeServer.xml.XMLServerConfigData;

public class ServerConfigData {

	private final int maxClients;

	private final int authTimeOutMillis;

	private final int rsaBitLength;

	private final int port;

	private final String adminEmail;

	private final int requestTimeOutMillis;

	private final int heartBeatTimeOutMillis;

	private final String clientJarURL;

	private final int webPort;

	private final int maxRetry;

	private final int cacheTimeOutSec;

	private final String restServiceLogin;

	private final String restServicePassword;

	private final String redisHost;

	private ServerConfigData(int maxClients, int authTimeOutMillis, int rsaBitLength, int port,
			int requestTimeOutMillis, int heartBeatTimeOutMillis, int webPort, int cacheTimeOutSec, String adminEmail,
			int maxRetry, String clientJarURL, String restServiceLogin, String restServicePassword, String redisHost)
			throws ServerConfigurationException {
		if (maxClients <= 0) {
			throw new ServerConfigurationException("'maxClients' value must be positive");
		}
		this.maxClients = maxClients;
		if (authTimeOutMillis <= 0) {
			throw new ServerConfigurationException("'authTimeOutMillis' value must be positive");
		}
		this.authTimeOutMillis = authTimeOutMillis;
		if (rsaBitLength <= 0) {
			throw new ServerConfigurationException("'rsaBitLength' value must be positive");
		}

		this.rsaBitLength = rsaBitLength;
		if (port <= 0) {
			throw new ServerConfigurationException("'port' value must be positive");
		} else if (port < 1024 || port > 65535) {
			throw new ServerConfigurationException("'port' must be in range [1024;65353]");
		}
		this.port = port;
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
		} else if (webPort < 1024 || webPort > 65535) {
			throw new ServerConfigurationException("'webPort' must be in range [1024;65353]");
		} else if (webPort == port) {
			throw new ServerConfigurationException("'webPort' and 'port' must not be equal");
		}
		this.webPort = webPort;
		if (cacheTimeOutSec <= 0) {
			throw new ServerConfigurationException("'cacheTimeOutSec' value must be positive");
		}
		this.cacheTimeOutSec = cacheTimeOutSec;

		if (adminEmail != null) {
			if (adminEmail.matches(RegularPatterns.EMAIL)) {
				this.adminEmail = adminEmail;
			} else {
				throw new ServerConfigurationException("'adminEmail' is not valid");
			}
		} else {
			this.adminEmail = adminEmail;
		}

		if (maxRetry <= 0) {
			throw new ServerConfigurationException("'maxRetry' value must be positive");
		}
		this.maxRetry = maxRetry;

		if (clientJarURL == null) {
			Server.getLogger().log(Level.WARNING, "'clientJarURL' was not specified. This may occure troubles.");
		}
		this.clientJarURL = clientJarURL;
		if (restServiceLogin == null) {
			throw new ServerConfigurationException("'restServiceLogin' must not be null");
		}
		this.restServiceLogin = restServiceLogin;
		if (restServicePassword == null) {
			throw new ServerConfigurationException("'restServicePassword' must not be null");
		}
		this.restServicePassword = restServicePassword;

		if (redisHost == null) {
			throw new ServerConfigurationException("'redisHost' must not be null");
		}
		this.redisHost = redisHost;
	}

	public ServerConfigData(JSONObject jsonObject) throws ServerConfigurationException {
		this(jsonObject.getInt("maxClients"), jsonObject.getInt("authTimeOutMillis"), jsonObject.getInt("rsaBitLength"),
				jsonObject.getInt("port"), jsonObject.getInt("requestTimeOutMillis"),
				jsonObject.getInt("heartBeatTimeOutMillis"), jsonObject.getInt("webPort"),
				jsonObject.getInt("cacheTimeOutSec"), jsonObject.getString("adminEmail"), jsonObject.getInt("maxRetry"),
				jsonObject.getString("clientJarURL"), jsonObject.getString("restServiceLogin"),
				jsonObject.getString("restServicePassword"), jsonObject.getString("redisHost"));
	}

	public int getAuthTimeOutMillis() {
		return authTimeOutMillis;
	}

	public int getRsaBitLength() {
		return rsaBitLength;
	}

	public int getPort() {
		return port;
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

	public int getCacheTimeOutSec() {
		return cacheTimeOutSec;
	}

	public int getMaxClients() {
		return maxClients;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public XMLServerConfigData toXML() {
		return new XMLServerConfigData(maxClients, authTimeOutMillis, rsaBitLength, port, requestTimeOutMillis,
				heartBeatTimeOutMillis, webPort, cacheTimeOutSec, adminEmail, maxRetry, clientJarURL);
	}

	public String getClientJarURL() {
		return clientJarURL;
	}

	public String getRestServiceLogin() {
		return restServiceLogin;
	}

	public String getRestServicePassword() {
		return restServicePassword;
	}

	public String getRedisHost() {
		return redisHost;
	}

	@Override
	public String toString() {
		return "ServerConfigData [maxClients=" + maxClients + ", authTimeOutMillis=" + authTimeOutMillis
				+ ", rsaBitLength=" + rsaBitLength + ", port=" + port + ", adminEmail=" + adminEmail
				+ ", requestTimeOutMillis=" + requestTimeOutMillis + ", heartBeatTimeOutMillis="
				+ heartBeatTimeOutMillis + ", clientJarURL=" + clientJarURL + ", webPort=" + webPort + ", maxRetry="
				+ maxRetry + ", cacheTimeOutSec=" + cacheTimeOutSec + ", restServiceLogin=" + restServiceLogin
				+ ", restServicePassword=" + restServicePassword + ", redisHost=" + redisHost + "]";
	}

}
