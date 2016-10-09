package ru.babobka.nodemasterserver.server;

import java.io.File;
import java.io.IOException;
import java.util.TimeZone;

import org.json.JSONException;

import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.listener.OnJSONExceptionListener;
import ru.babobka.nodemasterserver.model.ClientThreads;
import ru.babobka.nodemasterserver.runnable.HeartBeatingRunnable;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.nodemasterserver.thread.InputListenerThread;
import ru.babobka.nodemasterserver.webcontroller.AuthWebFilter;
import ru.babobka.nodemasterserver.webcontroller.AvailableTasksWebController;
import ru.babobka.nodemasterserver.webcontroller.CacheWebFilter;
import ru.babobka.nodemasterserver.webcontroller.CancelTaskWebController;
import ru.babobka.nodemasterserver.webcontroller.ClusterInfoWebController;
import ru.babobka.nodemasterserver.webcontroller.NodeUsersCRUDWebController;
import ru.babobka.nodemasterserver.webcontroller.StatisticsWebFilter;
import ru.babobka.nodemasterserver.webcontroller.TaskWebController;
import ru.babobka.nodemasterserver.webcontroller.TasksInfoWebController;

import ru.babobka.vsjws.webcontroller.WebFilter;
import ru.babobka.vsjws.webserver.WebServer;

/**
 * Created by dolgopolov.a on 16.07.15.
 */
public final class MasterServer extends Thread {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));
	}

	private final TaskPool taskPool = TaskPool.getInstance();

	private volatile Thread heartBeatingThread;

	private volatile Thread listenerThread;

	private volatile WebServer webServer;

	private static volatile MasterServer instance;

	private MasterServer() {

	}

	public static MasterServer getInstance() {
		MasterServer localInstance = instance;
		if (localInstance == null) {
			synchronized (MasterServer.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new MasterServer();
				}
			}
		}
		return localInstance;
	}

	@Override
	public void run() {
		try {
			if (!RedisDatasource.getInstance().getPool().getResource().isConnected()) {
				throw new IOException("Database is not connected");
			}

			listenerThread = new InputListenerThread(ServerContext.getInstance().getConfig().getMainServerPort());
			heartBeatingThread = new Thread(new HeartBeatingRunnable());
			webServer = new WebServer("rest server", ServerContext.getInstance().getConfig().getWebPort(), null,
					ServerContext.getInstance().getConfig().getLoggerFolder() + File.separator + "rest_log");
			WebFilter authWebFilter = new AuthWebFilter();
			WebFilter cacheWebFilter = new CacheWebFilter();
			WebFilter statisticsWebFilter = new StatisticsWebFilter();
			for (String taskName : taskPool.getTasksMap().keySet()) {
				webServer.addController("task/" + taskName, new TaskWebController().addWebFilter(authWebFilter)
						.addWebFilter(cacheWebFilter).addWebFilter(statisticsWebFilter));
			}
			webServer.addController("cancelTask", new CancelTaskWebController().addWebFilter(authWebFilter));
			webServer.addController("clusterInfo", new ClusterInfoWebController().addWebFilter(authWebFilter));
			webServer.addController("users", new NodeUsersCRUDWebController().addWebFilter(authWebFilter));
			webServer.addController("tasksInfo", new TasksInfoWebController().addWebFilter(authWebFilter));
			webServer.addController("availableTasks", new AvailableTasksWebController().addWebFilter(authWebFilter));
			webServer.addExceptionListener(JSONException.class, new OnJSONExceptionListener());

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

	private void clear() {
		WebServer localWebServer = webServer;
		if (localWebServer != null) {
			localWebServer.interrupt();
		}

		Thread localListenerThread = listenerThread;
		if (localListenerThread != null) {
			localListenerThread.interrupt();
			try {
				localListenerThread.join();
			} catch (InterruptedException e) {
				localListenerThread.interrupt();
				ServerContext.getInstance().getLogger().log(e);
			}
		}
		Thread localHeartBeatingThread = heartBeatingThread;
		if (localHeartBeatingThread != null) {
			localHeartBeatingThread.interrupt();
			try {
				localHeartBeatingThread.join();
			} catch (InterruptedException e) {
				localHeartBeatingThread.interrupt();
				ServerContext.getInstance().getLogger().log(e);

			}
		}
		ClientThreads clientThreads = ServerContext.getInstance().getClientThreads();
		if (clientThreads != null) {
			clientThreads.clear();
		}

	}

	public static void main(String[] args) {
		MasterServer server = new MasterServer();
		server.start();

	}

}