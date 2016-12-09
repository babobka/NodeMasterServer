package ru.babobka.nodemasterserver.builder;

import java.io.InputStream;

import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.ServerConfigurationException;
import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodemasterserver.util.StreamUtil;

public interface JSONFileServerConfigBuilder {

	public static MasterServerConfig build(InputStream is) {

		try {

			return new MasterServerConfig(new JSONObject(StreamUtil.readFile(is)));

		} catch (Exception e) {
			throw new ServerConfigurationException("Can not build server configuration", e);
		}

	}

}
