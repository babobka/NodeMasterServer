package ru.babobka.nodeServer.server;

import java.io.File;
import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Level;

import ru.babobka.nodeServer.model.ClientThreads;
import ru.babobka.nodeServer.model.ServerContext;
import ru.babobka.nodeServer.pool.FactoryPool;
import ru.babobka.nodeServer.runnable.HeartBeatingRunnable;
import ru.babobka.nodeServer.thread.InputListenerThread;
import ru.babobka.nodeServer.webController.AuthWebFilter;
import ru.babobka.nodeServer.webController.AvailableTasksWebController;
import ru.babobka.nodeServer.webController.CacheWebFilter;
import ru.babobka.nodeServer.webController.CancelTaskWebController;
import ru.babobka.nodeServer.webController.ClusterInfoWebController;
import ru.babobka.nodeServer.webController.NodeUsersCRUDWebController;
import ru.babobka.nodeServer.webController.StatisticsWebFilter;
import ru.babobka.nodeServer.webController.TaskWebController;
import ru.babobka.nodeServer.webController.TasksInfoWebController;
import ru.babobka.vsjws.webcontroller.WebFilter;
import ru.babobka.vsjws.webserver.WebServer;

/**
 * Created by dolgopolov.a on 16.07.15.
 */
public final class MasterServer {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));
	}

	private final FactoryPool factoryPool = FactoryPool.getInstance();

	private volatile Thread heartBeatingThread;

	private volatile Thread listenerThread;

	private volatile WebServer webServer;

	private volatile boolean running;

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

	void run() throws IOException {
		ServerContext.getInstance().getLogger().log("NodeServer runBlocking");
		try {
			factoryPool.init();
			listenerThread = new InputListenerThread(ServerContext.getInstance().getConfig().getMainServerPort());
			heartBeatingThread = new Thread(new HeartBeatingRunnable());
			listenerThread.start();
			heartBeatingThread.start();
			ServerContext.getInstance().getLogger().log(Level.INFO, "Node server start");
			webServer = new WebServer("rest server", ServerContext.getInstance().getConfig().getWebPort(), null,
					ServerContext.getInstance().getConfig().getLoggerFolder() + File.separator + "rest_log");
			WebFilter authWebFilter = new AuthWebFilter();
			WebFilter cacheWebFilter = new CacheWebFilter();
			WebFilter statisticsWebFilter = new StatisticsWebFilter();

			for (String taskName : factoryPool.getAvailableTasks().keySet()) {
				webServer.addController("/" + taskName, new TaskWebController().addWebFilter(authWebFilter)
						.addWebFilter(cacheWebFilter).addWebFilter(statisticsWebFilter));
			}
			webServer.addController("cancelTask", new CancelTaskWebController().addWebFilter(authWebFilter));
			webServer.addController("clusterInfo", new ClusterInfoWebController().addWebFilter(authWebFilter));
			webServer.addController("users", new NodeUsersCRUDWebController().addWebFilter(authWebFilter));
			webServer.addController("tasksInfo", new TasksInfoWebController().addWebFilter(authWebFilter));
			webServer.addController("availableTasks", new AvailableTasksWebController().addWebFilter(authWebFilter));
			webServer.run();
			running = true;
		} catch (Exception e) {
			running = false;
			stop();
			throw new IOException(e);
		}

	}

	void stop() {
		factoryPool.clear();
		WebServer localWebServer = webServer;
		if (localWebServer != null) {
			webServer.stop();
			webServer = null;
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
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isStopped() {
		WebServer localWebServer = webServer;
		Thread localListenerThread = listenerThread;
		Thread localHeartBeatingThread = heartBeatingThread;
		boolean listenerThreadIsNotAlive = localListenerThread == null || !localListenerThread.isAlive();
		boolean heartBeatingThreadNotAlive = localHeartBeatingThread == null || !localHeartBeatingThread.isAlive();
		boolean webServerIsStopped = localWebServer == null || !localWebServer.isRunning();
		boolean clientThreadsAreNotAlive = true;
		ClientThreads clientThreads = ServerContext.getInstance().getClientThreads();
		if (clientThreads != null) {
			clientThreadsAreNotAlive = clientThreads.isEmpty();
		}
		boolean allThreadsAreNotAlive = heartBeatingThreadNotAlive && listenerThreadIsNotAlive
				&& clientThreadsAreNotAlive;
		return !running && factoryPool.isEmpty() && allThreadsAreNotAlive && webServerIsStopped;
	}

	public static void main(String[] args) {
		MasterServer server = new MasterServer();
		ServerExecutor executor = new ServerExecutor(server);
		executor.run();
		// server.run();

	}

}