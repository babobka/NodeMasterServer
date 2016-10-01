package ru.babobka.nodeServer.builder;

import java.io.File;

import org.json.JSONObject;

import ru.babobka.nodeServer.exception.ServerConfigurationException;
import ru.babobka.nodeServer.model.ServerConfig;
import ru.babobka.nodeServer.util.StreamUtil;

public class JSONFileServerConfigBuilder {

	private JSONFileServerConfigBuilder() {

	}

	public static ServerConfig build() {
		File jsonFile = new File(
				JSONFileServerConfigBuilder.class.getClassLoader().getResource("config/config.json").getFile());

		try {
			return new ServerConfig(new JSONObject(StreamUtil.readFile(jsonFile)));

		} catch (Exception e) {
			throw new ServerConfigurationException(
					"Can not build server configuration using file " + jsonFile.getAbsolutePath(), e);
		}

	}

}
