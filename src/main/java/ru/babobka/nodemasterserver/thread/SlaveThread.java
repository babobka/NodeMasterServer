package ru.babobka.nodemasterserver.thread;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Level;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.exception.DistributionException;
import ru.babobka.nodemasterserver.exception.EmptyClusterException;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.AuthResult;
import ru.babobka.nodemasterserver.model.ResponseStorage;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodemasterserver.service.AuthService;
import ru.babobka.nodemasterserver.service.DistributionService;
import ru.babobka.nodemasterserver.service.NodeUsersService;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.RSA;

/**
 * Created by dolgopolov.a on 27.07.15.
 */
public class SlaveThread extends Thread implements Comparable<SlaveThread> {

	private final RSA rsa;

	private final Set<String> availableTasksSet = new HashSet<>();

	private static final int RSA_KEY_BIT_LENGTH = 256;

	private final NodeUsersService userService = Container.getInstance()
			.get(NodeUsersService.class);

	private final DistributionService distributionService = Container
			.getInstance().get(DistributionService.class);

	private final Slaves slaves = Container.getInstance().get(Slaves.class);

	private final AuthService authService = Container.getInstance()
			.get(AuthService.class);

	private final Map<UUID, NodeRequest> requestMap = new ConcurrentHashMap<>();

	private volatile String login;

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	private final ResponseStorage responseStorage = Container.getInstance()
			.get(ResponseStorage.class);

	private final MasterServerConfig masterServerConfig = Container
			.getInstance().get(MasterServerConfig.class);

	private final Socket socket;

	public SlaveThread(Socket socket) {
		if (socket != null) {
			logger.log("New connection " + socket);
			this.socket = socket;
			this.rsa = new RSA(RSA_KEY_BIT_LENGTH);
		} else {
			throw new IllegalArgumentException("Socket can not be null");
		}
	}

	public Set<String> getAvailableTasksSet() {
		return availableTasksSet;
	}

	public String getLogin() {
		return login;
	}

	public Socket getSocket() {
		return socket;
	}

	public Map<UUID, NodeRequest> getRequestMap() {
		return requestMap;
	}

	public synchronized void sendRequest(NodeRequest request)
			throws IOException {
		logger.log("sendRequest " + request);
		if (!(request.isRaceStyle()
				&& requestMap.containsKey(request.getTaskId()))) {
			requestMap.put(request.getRequestId(), request);
			StreamUtil.sendObject(request, socket);
			logger.log(request + " was sent");
			userService.incrementTaskCount(login);
		} else {
			logger.log(
					"Request  " + request + " was ignored due to race style");
			responseStorage.get(request.getTaskId())
					.add(NodeResponse.dummyResponse(request.getTaskId()));
		}
	}

	private synchronized void setBadAndCancelAllTheRequests() {
		if (!requestMap.isEmpty()) {
			NodeRequest request;
			for (Map.Entry<UUID, NodeRequest> requestEntry : requestMap
					.entrySet()) {
				request = requestEntry.getValue();
				responseStorage.addBadResponse(request.getTaskId());
				try {
					distributionService.broadcastStopRequests(
							slaves.getListByTaskId(request.getTaskId()),
							new NodeRequest(request.getTaskId(), true,
									request.getTaskName()));
				} catch (EmptyClusterException e) {
					logger.log(e);
				}
			}
			requestMap.clear();
		}
	}

	private synchronized void setBadAllTheRequests() {
		if (!requestMap.isEmpty()) {
			NodeRequest request;
			for (Map.Entry<UUID, NodeRequest> requestEntry : requestMap
					.entrySet()) {
				request = requestEntry.getValue();
				responseStorage.addBadResponse(request.getTaskId());
			}
			logger.log("Responses are clear");
			requestMap.clear();
		}
	}

	public void sendHeartBeating() throws IOException {

		StreamUtil.sendObject(NodeRequest.heartBeatRequest(), socket);

	}

	public synchronized void sendStopRequest(NodeRequest stopRequest)
			throws IOException {
		for (Map.Entry<UUID, NodeRequest> requestEntry : requestMap
				.entrySet()) {
			if (requestEntry.getValue().getTaskId()
					.equals(stopRequest.getTaskId())) {
				responseStorage.addStopResponse(stopRequest.getTaskId());
				requestMap.remove(requestEntry.getValue().getRequestId());
			}
		}
		StreamUtil.sendObject(stopRequest, socket);

	}

	public int getRequestCount() {
		return requestMap.size();
	}

	@Override
	public void run() {
		try {
			socket.setSoTimeout(masterServerConfig.getAuthTimeOutMillis());
			AuthResult authResult = authService.getAuthResult(rsa, socket);
			if (authResult.isValid()) {
				login = authResult.getLogin();
				if (!slaves.add(this)) {
					throw new IllegalStateException("Cluster is full");
				}
				if (authResult.getTaskSet() != null) {
					availableTasksSet.addAll(authResult.getTaskSet());
				}
				logger.log(login + " from " + socket + " was logged");

				while (!Thread.currentThread().isInterrupted()) {
					socket.setSoTimeout(
							masterServerConfig.getRequestTimeOutMillis());
					NodeResponse response = (NodeResponse) StreamUtil
							.receiveObject(socket);
					if (!response.isHeartBeatingResponse()) {
						logger.log("Got response " + response);
						requestMap.remove(response.getResponseId());
						logger.log(
								"Remove response " + response.getResponseId());
						if (responseStorage.exists(response.getTaskId())) {
							responseStorage.get(response.getTaskId())
									.add(response);
						}
					}
				}

			} else {
				logger.log(socket + " auth fail");
			}

		}

		catch (IOException e) {
			if (!Thread.currentThread().isInterrupted()) {
				logger.log(e);
			}
			logger.log(Level.WARNING, "Connection is closed " + socket);
		} catch (RuntimeException e) {
			logger.log(e);
		} finally {
			logger.log("Removing connection " + socket);
			slaves.remove(this);

			synchronized (SlaveThread.class) {
				if (!requestMap.isEmpty()) {
					logger.log("Slave has a requests to redistribute");
					try {
						distributionService.redistribute(this);
					} catch (DistributionException e) {
						logger.log(e);
						setBadAndCancelAllTheRequests();
					} catch (EmptyClusterException e) {
						logger.log(e);
						setBadAllTheRequests();
					}
				}

			}
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					logger.log(e);
				}
			}
			logger.log("User " + login + " was disconnected");

		}
	}

	public Map<String, LinkedList<NodeRequest>> getRequestsGroupedByTask() {
		Map<String, LinkedList<NodeRequest>> requestsByTaskName = new HashMap<>();
		for (Map.Entry<UUID, NodeRequest> requestEntry : this.getRequestMap()
				.entrySet()) {
			if (requestsByTaskName
					.containsKey(requestEntry.getValue().getTaskName())) {
				requestsByTaskName.get(requestEntry.getValue().getTaskName())
						.add(requestEntry.getValue());
			} else {
				requestsByTaskName.put(requestEntry.getValue().getTaskName(),
						new LinkedList<NodeRequest>());
				requestsByTaskName.get(requestEntry.getValue().getTaskName())
						.add(requestEntry.getValue());
			}
		}
		return requestsByTaskName;
	}

	@Override
	public String toString() {
		return "requests " + getRequestCount();
	}

	@Override
	public int compareTo(SlaveThread o) {
		if (o.getRequestCount() > this.getRequestCount()) {
			return -1;
		} else if (o.getRequestCount() < this.getRequestCount()) {
			return 1;
		}
		return 0;
	}

	@Override
	public void interrupt() {

		super.interrupt();

		try {
			socket.close();
		} catch (IOException e) {
			logger.log(e);
		}

	}

}