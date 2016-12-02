package ru.babobka.nodemasterserver.task;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import ru.babobka.nodemasterserver.exception.CanNotInitTaskFactoryException;
import ru.babobka.nodemasterserver.exception.EmptyFactoryPoolException;
import ru.babobka.nodemasterserver.exception.TaskNotFoundException;
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.subtask.model.SubTask;

/**
 * Created by dolgopolov.a on 09.07.15.
 */
public class TaskPool {
	private final Map<String, TaskContext> tasksMap = new ConcurrentHashMap<>();

	private static volatile TaskPool instance;

	private TaskPool() throws CanNotInitTaskFactoryException {
		init();
	}

	public static TaskPool getInstance() {
		TaskPool localInstance = instance;
		if (localInstance == null) {
			synchronized (TaskPool.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new TaskPool();
				}
			}
		}
		return localInstance;
	}

	private void init() throws CanNotInitTaskFactoryException {
		try {
			File tasksFolder = new File(MasterServerContext.getInstance().getConfig().getTasksFolder());
			String taskFolder = tasksFolder.getAbsolutePath();
			List<String> files = StreamUtil.getJarFileListFromFolder(taskFolder);
			for (String file : files) {
				try {
					String jarFilePath = taskFolder + File.separator + file;
					TaskConfig config = new TaskConfig(StreamUtil.getConfigJson(jarFilePath));
					SubTask subTask = StreamUtil.getTaskClassFromJar(jarFilePath, config.getClassName());
					tasksMap.put(config.getName(), new TaskContext(subTask, config));
				} catch (Exception e) {
					MasterServerContext.getInstance().getLogger().log(Level.SEVERE, "Can not init factory with file " + file);
					MasterServerContext.getInstance().getLogger().log(e);
					throw new CanNotInitTaskFactoryException(e);
				}
			}

		} catch (Exception e) {
			throw new CanNotInitTaskFactoryException(
					"Can not init factory pool. Try to redownload new jars to nodeserver task folder", e);
		}
		if (tasksMap.isEmpty()) {
			throw new CanNotInitTaskFactoryException(new EmptyFactoryPoolException(
					"Can not init factory pool. No task to run. Try to redownload new jars to nodeserver task folder"));
		}

	}

	public Map<String, TaskContext> getTasksMap() {
		return tasksMap;
	}

	public TaskContext get(String name) throws IOException {
		TaskContext taskContext = tasksMap.get(name);
		if (taskContext != null) {
			return taskContext.newInstance();
		} else {
			throw new TaskNotFoundException("Task " + name + " was not found");
		}

	}

	/*
	 * public void clear() { tasksMap.clear(); }
	 */

	public boolean isEmpty() {
		return tasksMap.isEmpty();
	}

}
