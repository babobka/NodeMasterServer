package ru.babobka.nodeServer.thread;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Level;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.exception.DistributionException;
import ru.babobka.nodeServer.exception.EmptyClusterException;
import ru.babobka.nodeServer.model.AuthResult;

import ru.babobka.nodeServer.service.AuthService;
import ru.babobka.nodeServer.service.AuthServiceImpl;
import ru.babobka.nodeServer.service.NodeUsersService;
import ru.babobka.nodeServer.service.NodeUsersServiceImpl;
import ru.babobka.nodeServer.util.DistributionUtil;
import ru.babobka.nodeServer.util.StreamUtil;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.RSA;

/**
 * Created by dolgopolov.a on 27.07.15.
 */
public class ClientThread extends Thread implements Comparable<ClientThread> {

	private final RSA rsa;

	private volatile Set<String> taskSet;

	private final NodeUsersService userService = NodeUsersServiceImpl.getInstance();

	private final AuthService authService = AuthServiceImpl.getInstance();

	private final Map<Long, NodeRequest> requestMap = new ConcurrentHashMap<>();

	private volatile String login;

	private static final Object REDISTRIBUTION_LOCK = new Object();

	private final Socket socket;

	public ClientThread(Socket socket) {
		if (socket != null) {
			this.setName("Client Thread " + new Date());
			Server.getLogger().log(Level.INFO, "New connection " + socket);
			this.socket = socket;
			this.rsa = new RSA(Server.getConfigData().getRsaBitLength());
		} else {
			throw new IllegalArgumentException("Socket can not be null");
		}
	}
	

	public Set<String> getTaskSet() {
		return taskSet;
	}

	public String getLogin() {
		return login;
	}

	public Socket getSocket() {
		return socket;
	}

	public Map<Long, NodeRequest> getRequestMap() {
		return requestMap;
	}

	public synchronized void sendRequest(NodeRequest request) throws IOException {

		if (!(request.isRaceStyle() && requestMap.containsKey(request.getTaskId()))) {
			StreamUtil.sendObject(request, socket);
			requestMap.put(request.getRequestId(), request);
			userService.incrementTaskCount(login);
			Server.getLogger().log(Level.INFO, "sendRequest " + request);
		} else {
			Server.getLogger().log(Level.INFO, "Request  " + request + " was ignored due to race style");
			Server.RESPONSE_STORAGE.get(request.getTaskId()).add(NodeResponse.dummyResponse(request.getTaskId()));
		}
	}

	private void setBadAndCancelAllTheRequests() {
		if (requestMap.size() != 0) {
			NodeRequest request;
			for (Map.Entry<Long, NodeRequest> requestEntry : requestMap.entrySet()) {
				request = requestEntry.getValue();
				Server.RESPONSE_STORAGE.setBadResponse(request.getTaskId());
				try {
					DistributionUtil.broadcastStopRequests(
							Server.getClientThreads().getListByTaskId(request.getTaskId()),
							new NodeRequest(request.getTaskId(), true, request.getTaskName()));
				} catch (EmptyClusterException e) {
					Server.getLogger().log(e);
				}
			}
			requestMap.clear();
		}
	}

	private void setBadAllTheRequests() {
		if (!requestMap.isEmpty()) {
			NodeRequest request;
			for (Map.Entry<Long, NodeRequest> requestEntry : requestMap.entrySet()) {
				request = requestEntry.getValue();
				Server.RESPONSE_STORAGE.setBadResponse(request.getTaskId());
			}
			requestMap.clear();
		}
	}

	public synchronized void sendHeartBeating() throws IOException {

		StreamUtil.sendObject(NodeRequest.heartBeatRequest(), socket);

	}

	public synchronized void sendStopRequest(NodeRequest stopRequest) throws IOException {
		try {
			StreamUtil.sendObject(stopRequest, socket);
		} finally {

			for (Map.Entry<Long, NodeRequest> requestEntry : requestMap.entrySet()) {
				if (requestEntry.getValue().getTaskId() == stopRequest.getTaskId()) {
					Server.RESPONSE_STORAGE.setStopResponse(stopRequest.getTaskId());
					requestMap.remove(requestEntry.getValue().getRequestId());
				}
			}

		}
	}

	public int getRequestCount() {
		return requestMap.size();
	}

	@Override
	public void run() {
		try {
			socket.setSoTimeout(Server.getConfigData().getAuthTimeOutMillis());
			AuthResult authResult = authService.getAuthResult(rsa, socket);

			if (authResult.isValid()) {
				if (!Server.getClientThreads().add(this)) {
					throw new RuntimeException("Cluster is full");
				}
				this.login = authResult.getLogin();
				this.taskSet = authResult.getTaskSet();
				Server.getLogger().log(Level.INFO, login + " from " + socket + " was logged");
				while (!Thread.currentThread().isInterrupted()) {
					socket.setSoTimeout(Server.getConfigData().getRequestTimeOutMillis());
					NodeResponse response = (NodeResponse) StreamUtil.receiveObject(socket);

					if (!response.isHeartBeatingResponse()) {
						Server.getLogger().log(Level.INFO, response.toString());
						requestMap.remove(response.getResponseId());
						if (Server.RESPONSE_STORAGE.exists(response.getTaskId())) {
							Server.RESPONSE_STORAGE.get(response.getTaskId()).add(response);
						}
					}
				}
			} else {
				Server.getLogger().log(Level.INFO, socket + " auth fail");
			}

		} catch (IOException e) {
			if (!socket.isClosed()) {
				Server.getLogger().log(e);
			}
			Server.getLogger().log(Level.WARNING, "Connection is closed " + socket + ". " + e.getMessage());
		} catch (Exception e) {
			Server.getLogger().log(Level.SEVERE, e);
		} finally {
			Server.getLogger().log(Level.INFO, "Removing connection " + socket);
			Server.getClientThreads().remove(this);
			synchronized (REDISTRIBUTION_LOCK) {
				if (!requestMap.isEmpty()) {
					try {
						DistributionUtil.redistribute(this);
					} catch (DistributionException e) {
						Server.getLogger().log(e);
						setBadAndCancelAllTheRequests();
					} catch (EmptyClusterException e) {
						Server.getLogger().log(e);
						setBadAllTheRequests();
					}
				}
			}
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					Server.getLogger().log(e);
				}
			}

		}
	}

	public Map<String, LinkedList<NodeRequest>> getRequestsGroupedByTask() {
		HashMap<String, LinkedList<NodeRequest>> requestsByTaskName = new HashMap<>();
		for (Map.Entry<Long, NodeRequest> requestEntry : this.getRequestMap().entrySet()) {
			if (requestsByTaskName.containsKey(requestEntry.getValue().getTaskName())) {
				requestsByTaskName.get(requestEntry.getValue().getTaskName()).add(requestEntry.getValue());
			} else {
				requestsByTaskName.put(requestEntry.getValue().getTaskName(), new LinkedList<NodeRequest>());
				requestsByTaskName.get(requestEntry.getValue().getTaskName()).add(requestEntry.getValue());
			}
		}
		return requestsByTaskName;
	}

	@Override
	public String toString() {
		return "requests " + getRequestCount();
	}

	@Override
	public int compareTo(ClientThread o) {
		if (o.getRequestCount() > this.getRequestCount()) {
			return -1;
		} else if (o.getRequestCount() < this.getRequestCount()) {
			return 1;
		}
		return 0;
	}

}