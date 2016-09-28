package ru.babobka.nodeServer.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "serverConfigData")
public class XMLServerConfigData {

	private int maxClients;

	private int authTimeOutMillis;

	private int rsaBitLength;

	private int port;

	private int requestTimeOutMillis;

	private int heartBeatTimeOutMillis;

	private int webPort;

	private int cacheTimeOutSec;

	private String adminEmail;

	private int maxRetry;

	private String clientJarURL;

	public XMLServerConfigData() {
		// xml transformer requires default contructor
	}

	public XMLServerConfigData(int maxClients, int authTimeOutMillis, int rsaBitLength, int port,
			int requestTimeOutMillis, int heartBeatTimeOutMillis, int webPort, int cacheTimeOutSec, String adminEmail,
			int maxRetry, String clientJarURL) {

		this.maxClients = maxClients;
		this.authTimeOutMillis = authTimeOutMillis;
		this.rsaBitLength = rsaBitLength;
		this.port = port;
		this.requestTimeOutMillis = requestTimeOutMillis;
		this.heartBeatTimeOutMillis = heartBeatTimeOutMillis;
		this.webPort = webPort;
		this.cacheTimeOutSec = cacheTimeOutSec;
		this.adminEmail = adminEmail;
		this.maxRetry = maxRetry;
		this.clientJarURL = clientJarURL;
	}

	public int getMaxClients() {
		return maxClients;
	}

	public void setMaxClients(int maxClients) {
		this.maxClients = maxClients;
	}

	public int getAuthTimeOutMillis() {
		return authTimeOutMillis;
	}

	public void setAuthTimeOutMillis(int authTimeOutMillis) {
		this.authTimeOutMillis = authTimeOutMillis;
	}

	public int getRsaBitLength() {
		return rsaBitLength;
	}

	public void setRsaBitLength(int rsaBitLength) {
		this.rsaBitLength = rsaBitLength;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getRequestTimeOutMillis() {
		return requestTimeOutMillis;
	}

	public void setRequestTimeOutMillis(int requestTimeOutMillis) {
		this.requestTimeOutMillis = requestTimeOutMillis;
	}

	public int getHeartBeatTimeOutMillis() {
		return heartBeatTimeOutMillis;
	}

	public void setHeartBeatTimeOutMillis(int heartBeatTimeOutMillis) {
		this.heartBeatTimeOutMillis = heartBeatTimeOutMillis;
	}

	public int getWebPort() {
		return webPort;
	}

	public void setWebPort(int webPort) {
		this.webPort = webPort;
	}

	public int getCacheTimeOutSec() {
		return cacheTimeOutSec;
	}

	public void setCacheTimeOutSec(int cacheTimeOutSec) {
		this.cacheTimeOutSec = cacheTimeOutSec;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getClientJarURL() {
		return clientJarURL;
	}

	public void setClientJarURL(String clientJarURL) {
		this.clientJarURL = clientJarURL;
	}

}
