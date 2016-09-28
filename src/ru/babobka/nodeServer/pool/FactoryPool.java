package ru.babobka.nodeServer.pool;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.exception.CanNotInitTaskFactoryException;
import ru.babobka.nodeServer.exception.EmptyFactoryPoolException;
import ru.babobka.nodeServer.util.StreamUtil;
import ru.babobka.subtask.model.SubTask;

/**
 * Created by dolgopolov.a on 09.07.15.
 */
public class FactoryPool {
	private final HashMap<String, Class<?>> poolMap = new HashMap<>();

	private final HashMap<String, String> availableTasks = new HashMap<>();

	private static volatile FactoryPool instance;

	private FactoryPool() {

	}

	public static FactoryPool getInstance() {
		FactoryPool localInstance = instance;
		if (localInstance == null) {
			synchronized (FactoryPool.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new FactoryPool();
				}
			}
		}
		return localInstance;
	}

	public void init() throws CanNotInitTaskFactoryException,
			EmptyFactoryPoolException {
		try {
			String taskFolder = Server.getRunningFolder() + File.separator
					+ "tasks";
			List<String> files = StreamUtil.getFileListFromFolder(taskFolder);
			for (String file : files) {
				try {
					Class<?> clazz = StreamUtil.getTaskClassFromJar(taskFolder
							+ File.separator + file);
					SubTask subTask = (SubTask) clazz.newInstance();
					poolMap.put(subTask.getTaskName(), clazz);
					availableTasks.put(subTask.getTaskName(),
							subTask.getDescription());
				} catch (Exception e) {
					Server.getLogger().log(Level.SEVERE, e);
					Server.getLogger().log(Level.SEVERE,
							"Can not init factoryPool");
				}
			}

		} catch (Exception e) {
			throw new CanNotInitTaskFactoryException(
					"Can not init factory pool. Try to redownload new jars to nodeserver task folder",
					e);
		}
		if (poolMap.isEmpty()) {
			throw new EmptyFactoryPoolException(
					"Can not init factory pool. No task to run. Try to redownload new jars to nodeserver task folder");
		}

	}

	public Map<String, String> getAvailableTasks() {
		return availableTasks;
	}

	public SubTask get(String uri) throws IllegalAccessException,
			InstantiationException {

		return (SubTask) poolMap.get(uri).newInstance();

	}

}
