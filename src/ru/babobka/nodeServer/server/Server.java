package ru.babobka.nodeServer.server;

import java.io.File;
import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Level;

import ru.babobka.nodeServer.model.ClientThreads;
import ru.babobka.nodeServer.model.ServerContext;
import ru.babobka.nodeServer.pool.FactoryPool;
import ru.babobka.nodeServer.runnable.HeartBeatingRunnable;
import ru.babobka.nodeServer.runnable.InputListenerThread;
import ru.babobka.nodeServer.webController.AuthWebFilter;
import ru.babobka.nodeServer.webController.AvailableTasksWebController;
import ru.babobka.nodeServer.webController.CacheWebFilter;
import ru.babobka.nodeServer.webController.CancelTaskWebController;
import ru.babobka.nodeServer.webController.ClusterInfoWebController;
import ru.babobka.nodeServer.webController.MainPageWebController;
import ru.babobka.nodeServer.webController.NodeUsersCRUDWebController;
import ru.babobka.nodeServer.webController.StatisticsWebFilter;
import ru.babobka.nodeServer.webController.TaskWebController;
import ru.babobka.nodeServer.webController.TasksInfoWebController;
import ru.babobka.vsjws.webcontroller.WebFilter;
import ru.babobka.vsjws.webserver.WebServer;

/**
 * Created by dolgopolov.a on 16.07.15.
 */
public final class Server {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));
	}

	private final FactoryPool factoryPool = FactoryPool.getInstance();

	private volatile Thread heartBeatingThread;

	private volatile Thread listenerThread;

	private volatile WebServer webServer;

	private volatile boolean running;

	private volatile Thread runThread;

	private static volatile Server instance;

	private volatile boolean starting;

	private volatile boolean stopping;

	private Server() {

	}

	public static Server getInstance() {
		Server localInstance = instance;
		if (localInstance == null) {
			synchronized (Server.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new Server();
				}
			}
		}
		return localInstance;
	}

	public void run() {
		if (!running && !starting) {
			synchronized (this) {
				if (!running && !starting) {
					ServerContext.getInstance().getLogger().log("NodeServer run");
					starting = true;
					if (runThread != null && runThread.isAlive()) {
						try {
							runThread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					runThread = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								runBlocking();
							} catch (Exception e) {
								e.printStackTrace();
								stop();
								running = false;
							}

						}
					});
					runThread.start();
				}
			}
		}
	}

	private void runBlocking() throws IOException {
		ServerContext.getInstance().getLogger().log("NodeServer runBlocking");
		try {
			factoryPool.init();
			listenerThread = new InputListenerThread(ServerContext.getInstance().getConfig().getPort());
			heartBeatingThread = new Thread(new HeartBeatingRunnable());
			listenerThread.start();
			heartBeatingThread.start();
			ServerContext.getInstance().getLogger().log(Level.INFO, "Node server start");
			webServer = new WebServer("rest server", ServerContext.getInstance().getConfig().getWebPort(),
					ServerContext.getInstance().getRunningFolder() + File.separator + "web-content",
					ServerContext.getInstance().getRunningFolder() + File.separator + "rest_log");
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
			webServer.addController("", new MainPageWebController());
			webServer.run();
			running = true;
		} catch (Exception e) {
			running = false;
			stopBrutal();
			throw new IOException(e);
		} finally {
			starting = false;
		}

	}

	private void stopBrutal() {
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
				ServerContext.getInstance().getLogger().log(e);
			}
		}
		Thread localHeartBeatingThread = heartBeatingThread;
		if (localHeartBeatingThread != null) {
			localHeartBeatingThread.interrupt();
			try {
				localHeartBeatingThread.join();
			} catch (InterruptedException e) {
				ServerContext.getInstance().getLogger().log(e);
			}
		}
		ClientThreads clientThreads = ServerContext.getInstance().getClientThreads();
		if (clientThreads != null) {
			clientThreads.clear();
		}
		running = false;
	}

	public void stop() {
		while (starting) {
			Thread.yield();
		}

		if (running) {
			synchronized (this) {
				if (running) {
					try {
						stopping = true;
						ServerContext.getInstance().getLogger().log("Try to stop NodeServer");
						stopBrutal();
					} finally {
						stopping = false;
					}
				}
			}
		}
	}

	public boolean isRunning() {
		while (starting || stopping) {
			Thread.yield();
		}
		return running;
	}

	public boolean isStopped() {
		while (starting || stopping) {
			Thread.yield();
		}
		WebServer localWebServer = webServer;
		Thread localListenerThread = listenerThread;
		Thread localHeartBeatingThread = heartBeatingThread;
		boolean listenerThreadIsInterrupted = localListenerThread == null || !localListenerThread.isAlive();
		boolean heartBeatingThreadInterrupted = localHeartBeatingThread == null || !localHeartBeatingThread.isAlive();
		boolean webServerIsStopped = localWebServer == null || !localWebServer.isRunning();
		boolean clientThreadsAreInterrupted = true;
		ClientThreads clientThreads = ServerContext.getInstance().getClientThreads();
		if (clientThreads != null) {
			clientThreadsAreInterrupted = clientThreads.isEmpty();
		}
		return !running && heartBeatingThreadInterrupted && factoryPool.isEmpty() && listenerThreadIsInterrupted
				&& webServerIsStopped && clientThreadsAreInterrupted;
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

}