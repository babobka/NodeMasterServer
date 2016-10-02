package ru.babobka.nodemasterserver.builder;

import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.ServerConfigurationException;
import ru.babobka.nodemasterserver.server.ServerConfig;
import ru.babobka.nodemasterserver.util.StreamUtil;

public class JSONFileServerConfigBuilder {

	private static final String PRODUCTION_CONFIG = "config_production.json";

	private static final String TEST_CONFIG = "config_test.json";

	private JSONFileServerConfigBuilder() {

	}

	public static ServerConfig build(boolean production) {

		try {
			String config;
			if (production) {
				config = PRODUCTION_CONFIG;
			} else {
				config = TEST_CONFIG;
			}
			return new ServerConfig(new JSONObject(StreamUtil
					.readFile(JSONFileServerConfigBuilder.class.getClassLoader().getResourceAsStream(config))));

		} catch (Exception e) {
			throw new ServerConfigurationException("Can not build server configuration", e);
		}

	}

}
