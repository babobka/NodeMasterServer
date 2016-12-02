package ru.babobka.nodemasterserver.builder;

import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.ServerConfigurationException;
import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodemasterserver.util.StreamUtil;

public interface JSONFileServerConfigBuilder {

	public static MasterServerConfig build(String configFolder) {

		try {

			return new MasterServerConfig(new JSONObject(StreamUtil.readFile(configFolder)));

		} catch (Exception e) {
			throw new ServerConfigurationException("Can not build server configuration", e);
		}

	}

}
