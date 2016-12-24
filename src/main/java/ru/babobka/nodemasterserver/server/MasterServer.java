package ru.babobka.nodemasterserver.server;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;

import ru.babobka.container.Container;
import ru.babobka.container.ContainerStrategyException;
import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.exception.TaskNotFoundException;
import ru.babobka.nodemasterserver.listener.OnIllegalArgumentExceptionListener;
import ru.babobka.nodemasterserver.listener.OnIllegalStateExceptionListener;
import ru.babobka.nodemasterserver.listener.OnJSONExceptionListener;
import ru.babobka.nodemasterserver.listener.OnTaskNotFoundExceptionListener;
import ru.babobka.nodemasterserver.listener.OnTimeoutExceptionListener;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.runnable.HeartBeatingRunnable;
import ru.babobka.nodemasterserver.service.NodeUsersService;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.nodemasterserver.thread.InputListenerThread;
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

	public static final String MASTER_SERVER_TEST_CONFIG = "master_config.json";

	private final NodeUsersService userService = Container.getInstance()
			.get(NodeUsersService.class);

	private final TaskPool taskPool = Container.getInstance()
			.get(TaskPool.class);

	private final Thread heartBeatingThread;

	private final Thread listenerThread;

	private final WebServer webServer;

	private final Slaves slaves = Container.getInstance().get(Slaves.class);

	private final MasterServerConfig config = Container.getInstance()
			.get(MasterServerConfig.class);

	private final RedisDatasource datasource = Container.getInstance()
			.get(RedisDatasource.class);

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	public MasterServer() throws IOException {

		if (!config.isDebugDataBase()
				&& !datasource.getPool().getResource().isConnected()) {
			throw new IOException("Database is not connected");
		}
		if (!config.isProductionDataBase()) {
			userService.addTestUser();
		}

		listenerThread = new InputListenerThread(config.getMainServerPort());
		heartBeatingThread = new Thread(new HeartBeatingRunnable());
		webServer = new WebServer("rest server", config.getWebPort(),
				config.getLoggerFolder() + File.separator + "rest_log");

		WebFilter authWebFilter = new AuthWebFilter();
		WebFilter cacheWebFilter = new CacheWebFilter();
		for (String taskName : taskPool.getTasksMap().keySet()) {
			webServer.addController(
					"task/" + URLEncoder.encode(taskName, "UTF-8"),
					new TaskWebController().addWebFilter(authWebFilter)
							.addWebFilter(cacheWebFilter));
		}
		webServer.addController("cancelTask",
				new CancelTaskWebController().addWebFilter(authWebFilter));
		webServer.addController("clusterInfo",
				new ClusterInfoWebController().addWebFilter(authWebFilter));
		webServer.addController("users",
				new NodeUsersCRUDWebController().addWebFilter(authWebFilter));
		webServer.addController("tasksInfo",
				new TasksInfoWebController().addWebFilter(authWebFilter));
		webServer.addController("availableTasks",
				new AvailableTasksWebController().addWebFilter(authWebFilter));

		webServer.addExceptionListener(JSONException.class,
				new OnJSONExceptionListener());
		webServer.addExceptionListener(IllegalArgumentException.class,
				new OnIllegalArgumentExceptionListener());
		webServer.addExceptionListener(IllegalStateException.class,
				new OnIllegalStateExceptionListener());
		webServer.addExceptionListener(TaskNotFoundException.class,
				new OnTaskNotFoundExceptionListener());
		webServer.addExceptionListener(TimeoutException.class,
				new OnTimeoutExceptionListener());
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
		if (slaves != null) {
			slaves.clear();
		}
		if (datasource != null) {
			datasource.getPool().close();
		}

	}

	private void interruptAndJoin(Thread thread) {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			thread.interrupt();
			logger.log(e);
		}

	}

	public static void main(String[] args)
			throws IOException, ContainerStrategyException {

		MasterServer server = new MasterServer();
		server.start();

	}

}