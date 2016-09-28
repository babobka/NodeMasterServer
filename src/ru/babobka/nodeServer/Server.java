package ru.babobka.nodeServer;

import ru.babobka.nodeServer.builder.JSONFileServerConfigBuilder;
import ru.babobka.nodeServer.model.ClientThreads;
import ru.babobka.nodeServer.model.ResponseStorage;
import ru.babobka.nodeServer.model.ServerConfigData;
import ru.babobka.nodeServer.pool.FactoryPool;
import ru.babobka.nodeServer.runnable.HeartBeatingRunnable;
import ru.babobka.nodeServer.runnable.InputListenerRunnable;
import ru.babobka.nodeServer.util.StreamUtil;
import ru.babobka.nodeServer.webController.AuthWebFilter;
import ru.babobka.nodeServer.webController.AvailableTasksWebController;
import ru.babobka.nodeServer.webController.CacheWebFilter;
import ru.babobka.nodeServer.webController.CancelTaskWebController;
import ru.babobka.nodeServer.webController.ClusterInfoWebController;
import ru.babobka.nodeServer.webController.GetStartTimeWebController;
import ru.babobka.nodeServer.webController.MainPageWebController;
import ru.babobka.nodeServer.webController.NodeUsersCRUDWebController;
import ru.babobka.nodeServer.webController.StatisticsWebFilter;
import ru.babobka.nodeServer.webController.TaskWebController;
import ru.babobka.nodeServer.webController.TasksInfoWebController;
import ru.babobka.vsjsw.logger.SimpleLogger;
import ru.babobka.vsjws.webserver.WebServer;
import ru.babobka.vsjws.webcontroller.WebFilter;

import java.io.File;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Created by dolgopolov.a on 16.07.15.
 */
public final class Server {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));
	}

	private static final FactoryPool factoryPool = FactoryPool.getInstance();

	private static ClientThreads clientThreads;

	public static final ResponseStorage RESPONSE_STORAGE = new ResponseStorage();

	private static final Thread HEART_BEATING_THREAD = new Thread(new HeartBeatingRunnable());

	public static final long START_TIME = System.currentTimeMillis();

	private static SimpleLogger nodeLogger;

	private static String runningFolder;

	private static ServerConfigData config;

	private Server() {

	}

	public static String getRunningFolder() {
		return runningFolder;
	}

	public static ClientThreads getClientThreads() {
		return clientThreads;
	}

	public static SimpleLogger getLogger() {
		return nodeLogger;
	}

	public static ServerConfigData getConfigData() {
		return config;
	}

	public static void main(String[] args) {

		try {
			runningFolder = StreamUtil.getRunningFolder();
			nodeLogger = new SimpleLogger("NodeServer", runningFolder, "server");
			factoryPool.init();
			config = JSONFileServerConfigBuilder.build();
			clientThreads = new ClientThreads(config.getMaxClients());
			Thread listenerThread = new Thread(new InputListenerRunnable(config.getPort()));
			listenerThread.setDaemon(true);
			listenerThread.start();
			HEART_BEATING_THREAD.setDaemon(true);
			HEART_BEATING_THREAD.start();
			nodeLogger.log(Level.INFO, "Node server start");
			WebServer webServer = new WebServer("rest server", config.getWebPort(),
					runningFolder + File.separator + "web-content", runningFolder + File.separator + "rest_log");
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
			webServer.addController("startTime", new GetStartTimeWebController().addWebFilter(authWebFilter));
			webServer.addController("", new MainPageWebController());
			webServer.run();

		} catch (Exception e) {
			nodeLogger.log(Level.SEVERE, e);
		}
	}

}