package ru.babobka.nodeServer.builder;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import ru.babobka.nodeServer.exception.ServerConfigurationException;
import ru.babobka.nodeServer.model.ServerConfig;
import ru.babobka.nodeServer.util.StreamUtil;

public interface JSONFileServerConfigBuilder {

	public static ServerConfig build(String runningFolder) throws ServerConfigurationException {
		String configFilePath = runningFolder + File.separator + "config" + File.separator + "config.json";
		File jsonFile = new File(configFilePath);
		if (jsonFile.exists()) {
			try {
				return new ServerConfig(new JSONObject(StreamUtil.readFile(configFilePath)));

			} catch (JSONException e) {
				throw new ServerConfigurationException(
						"Can not build server configuration using file " + configFilePath, e);
			}
		} else {
			throw new ServerConfigurationException(
					"Can not find config file " + configFilePath + ". Redownload this file or create your own.");
		}
	}
}
