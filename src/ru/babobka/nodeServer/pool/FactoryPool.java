package ru.babobka.nodeServer.pool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ru.babobka.nodeServer.builder.JSONFileServerConfigBuilder;
import ru.babobka.nodeServer.exception.CanNotInitTaskFactoryException;
import ru.babobka.nodeServer.exception.EmptyFactoryPoolException;
import ru.babobka.nodeServer.model.ServerContext;
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

	public synchronized void init() throws CanNotInitTaskFactoryException {
		try {
			File tasksFolder = new File(ServerContext.getInstance().getConfig().getTasksFolder());
			String taskFolder = tasksFolder.getAbsolutePath();
			List<String> files = StreamUtil.getJarFileListFromFolder(taskFolder);
			for (String file : files) {
				try {

					Class<?> clazz = StreamUtil.getTaskClassFromJar(taskFolder + File.separator + file);
					SubTask subTask = (SubTask) clazz.newInstance();
					poolMap.put(subTask.getTaskName(), clazz);
					availableTasks.put(subTask.getTaskName(), subTask.getDescription());
				} catch (Exception e) {
					ServerContext.getInstance().getLogger().log(Level.SEVERE, "Can not init factory with file " + file);
					ServerContext.getInstance().getLogger().log(e);
					throw new CanNotInitTaskFactoryException(e);
				}
			}

		} catch (Exception e) {
			throw new CanNotInitTaskFactoryException(
					"Can not init factory pool. Try to redownload new jars to nodeserver task folder", e);
		}
		if (poolMap.isEmpty()) {
			throw new CanNotInitTaskFactoryException(new EmptyFactoryPoolException(
					"Can not init factory pool. No task to run. Try to redownload new jars to nodeserver task folder"));
		}

	}

	public synchronized Map<String, String> getAvailableTasks() {
		return availableTasks;
	}

	public synchronized SubTask get(String uri) throws IOException {
		try {
			return (SubTask) poolMap.get(uri).newInstance();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public synchronized void clear() {
		poolMap.clear();
		availableTasks.clear();
	}

	public synchronized boolean isEmpty() {
		return poolMap.isEmpty() && availableTasks.isEmpty();
	}

}
