package ru.babobka.nodeServer.builder;


import org.json.JSONObject;

import ru.babobka.nodeServer.exception.ServerConfigurationException;
import ru.babobka.nodeServer.model.ServerConfig;
import ru.babobka.nodeServer.util.StreamUtil;

public class JSONFileServerConfigBuilder {

	private JSONFileServerConfigBuilder() {

	}

	public static ServerConfig build() {

		try {
			return new ServerConfig(new JSONObject(StreamUtil.readFile(
					JSONFileServerConfigBuilder.class.getClassLoader().getResourceAsStream("config/config.json"))));

		} catch (Exception e) {
			throw new ServerConfigurationException("Can not build server configuration", e);
		}

	}

}
