package ru.babobka.nodemasterserver.builder;

import java.io.File;

import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.ServerConfigurationException;
import ru.babobka.nodemasterserver.server.ServerConfig;
import ru.babobka.nodemasterserver.util.StreamUtil;

public class JSONFileServerConfigBuilder {

	private static final String CONFIG = "master_config.json";

	private JSONFileServerConfigBuilder() {

	}

	public static ServerConfig build(boolean production) {

		try {
			if (production) {
				return new ServerConfig(
						new JSONObject(StreamUtil.readFile(StreamUtil.getRunningFolder() + File.separator + CONFIG)));
			} else {
				return new ServerConfig(new JSONObject(StreamUtil
						.readFile(JSONFileServerConfigBuilder.class.getClassLoader().getResourceAsStream(CONFIG))));
			}

		} catch (Exception e) {
			throw new ServerConfigurationException("Can not build server configuration", e);
		}

	}

}
