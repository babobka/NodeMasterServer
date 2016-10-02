package ru.babobka.nodemasterserver.builder;


import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.ServerConfigurationException;
import ru.babobka.nodemasterserver.model.ServerConfig;
import ru.babobka.nodemasterserver.util.StreamUtil;


public class JSONFileServerConfigBuilder {

	private JSONFileServerConfigBuilder() {

	}

	public static ServerConfig build() {

		try {
			return new ServerConfig(new JSONObject(StreamUtil.readFile(
					JSONFileServerConfigBuilder.class.getClassLoader().getResourceAsStream("config.json"))));

		} catch (Exception e) {
			throw new ServerConfigurationException("Can not build server configuration", e);
		}

	}

}
