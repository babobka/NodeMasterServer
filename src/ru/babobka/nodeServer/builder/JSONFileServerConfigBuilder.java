package ru.babobka.nodeServer.builder;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.exception.ServerConfigurationException;
import ru.babobka.nodeServer.model.ServerConfigData;
import ru.babobka.nodeServer.util.StreamUtil;

public interface JSONFileServerConfigBuilder {

	public static ServerConfigData build() throws ServerConfigurationException {
		String configFilePath = Server.getRunningFolder() + File.separator + "config" + File.separator + "config.json";
		File jsonFile = new File(configFilePath);
		if (jsonFile.exists()) {
			try {
				return new ServerConfigData(new JSONObject(StreamUtil.readFile(configFilePath)));

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
