package ru.babobka.nodemasterserver.server;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.exception.TaskNotFoundException;
import ru.babobka.nodemasterserver.listener.OnIllegalArgumentExceptionListener;
import ru.babobka.nodemasterserver.listener.OnIllegalStateExceptionListener;
import ru.babobka.nodemasterserver.listener.OnJSONExceptionListener;
import ru.babobka.nodemasterserver.listener.OnTaskNotFoundExceptionListener;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.runnable.HeartBeatingRunnable;
import ru.babobka.nodemasterserver.service.NodeUsersService;
import ru.babobka.nodemasterserver.service.NodeUsersServiceImpl;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.nodemasterserver.thread.InputListenerThread;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodemasterserver.webcontroller.AuthWebFilter;
import ru.babobka.nodemasterserver.webcontroller.AvailableTasksWebController;
import ru.babobka.nodemasterserver.webcontroller.CacheWebFilter;
import ru.babobka.nodemasterserver.webcontroller.CancelTaskWebController;
import ru.babobka.nodemasterserver.webcontroller.ClusterInfoWebController;
import ru.babobka.nodemasterserver.webcontroller.NodeUsersCRUDWebController;
import ru.babobka.nodemasterserver.webcontroller.TaskWebController;
import ru.babobka.nodemasterserver.webcontroller.TasksInfoWebController;

import ru.babobka.vsjws.webcontroller.WebFilter;
import ru.babobka.vsjws.webserver.WebServer;

/**
 * Created by dolgopolov.a on 16.07.15.
 */
public final class MasterServer extends Thread {

	private final NodeUsersService userService = NodeUsersServiceImpl.getInstance();

	private final TaskPool taskPool = TaskPool.getInstance();

	private final Thread heartBeatingThread;

	private final Thread listenerThread;

	private final WebServer webServer;

	private static volatile MasterServer instance;

	private MasterServer() throws IOException {

		MasterServerContext masterServerContext = MasterServerContext.getInstance();

		if (!MasterServerContext.getInstance().getConfig().isDebugDataBase()
				&& !RedisDatasource.getInstance().getPool().getResource().isConnected()) {
			throw new IOException("Database is not connected");
		}
		if (!MasterServerContext.getInstance().getConfig().isProductionDataBase()) {
			userService.addTestUser();
		}

		listenerThread = new InputListenerThread(masterServerContext.getConfig().getMainServerPort());
		heartBeatingThread = new Thread(new HeartBeatingRunnable());
		webServer = new WebServer("rest server", masterServerContext.getConfig().getWebPort(),
				masterServerContext.getConfig().getLoggerFolder() + File.separator + "rest_log");

		WebFilter authWebFilter = new AuthWebFilter();
		WebFilter cacheWebFilter = new CacheWebFilter();
		for (String taskName : taskPool.getTasksMap().keySet()) {
			webServer.addController("task/" + URLEncoder.encode(taskName, "UTF-8"),
					new TaskWebController().addWebFilter(authWebFilter).addWebFilter(cacheWebFilter));
		}
		webServer.addController("cancelTask", new CancelTaskWebController().addWebFilter(authWebFilter));
		webServer.addController("clusterInfo", new ClusterInfoWebController().addWebFilter(authWebFilter));
		webServer.addController("users", new NodeUsersCRUDWebController().addWebFilter(authWebFilter));
		webServer.addController("tasksInfo", new TasksInfoWebController().addWebFilter(authWebFilter));
		webServer.addController("availableTasks", new AvailableTasksWebController().addWebFilter(authWebFilter));

		webServer.addExceptionListener(JSONException.class, new OnJSONExceptionListener());
		webServer.addExceptionListener(IllegalArgumentException.class, new OnIllegalArgumentExceptionListener());
		webServer.addExceptionListener(IllegalStateException.class, new OnIllegalStateExceptionListener());
		webServer.addExceptionListener(TaskNotFoundException.class, new OnTaskNotFoundExceptionListener());
	}

	public static MasterServer getInstance() throws IOException {
		MasterServer localInstance = instance;
		if (localInstance == null || !localInstance.isAlive()) {
			synchronized (MasterServer.class) {
				localInstance = instance;
				if (localInstance == null || !localInstance.isAlive()) {
					instance = localInstance = new MasterServer();
				}
			}
		}
		return localInstance;
	}

	@Override
	public void run() {
		try {
			listenerThread.start();
			heartBeatingThread.start();
			webServer.start();
		} catch (Exception e) {
			clear();
		}

	}

	@Override
	public void interrupt() {
		super.interrupt();
		clear();
	}

	private synchronized void clear() {
		interruptAndJoin(webServer);
		interruptAndJoin(listenerThread);
		interruptAndJoin(listenerThread);
		Slaves slaves = MasterServerContext.getInstance().getSlaves();
		if (slaves != null) {
			slaves.clear();
		}

	}

	private static void interruptAndJoin(Thread thread) {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			thread.interrupt();
			MasterServerContext.getInstance().getLogger().log(e);
		}

	}

	public static void main(String[] args) throws IOException {
		MasterServerContext.setConfigPath(StreamUtil.getLocalResourcePath(MasterServer.class, "master_config.json"));
		MasterServer server = new MasterServer();
		server.start();
	}

}