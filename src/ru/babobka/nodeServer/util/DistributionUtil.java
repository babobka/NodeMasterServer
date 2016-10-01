package ru.babobka.nodeServer.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ru.babobka.nodeServer.exception.DistributionException;
import ru.babobka.nodeServer.exception.EmptyClusterException;
import ru.babobka.nodeServer.model.ServerContext;
import ru.babobka.nodeServer.thread.ClientThread;
import ru.babobka.nodeserials.NodeRequest;

public final class DistributionUtil {

	private static final int MAX_RETRY = 5;

	private DistributionUtil() {

	}

	public static void redistribute(ClientThread clientThread) throws DistributionException, EmptyClusterException {

		if (clientThread.getRequestMap().size() > 0) {
			if (!ServerContext.getInstance().getClientThreads().isEmpty()) {
				ServerContext.getInstance().getLogger().log(Level.INFO, "Redistribution");
				Map<String, LinkedList<NodeRequest>> requestsByUri = clientThread.getRequestsGroupedByTask();
				for (Map.Entry<String, LinkedList<NodeRequest>> requestByUriEntry : requestsByUri.entrySet()) {
					try {
						broadcastRequests(requestByUriEntry.getKey(), requestByUriEntry.getValue(), MAX_RETRY);
					} catch (Exception e) {
						ServerContext.getInstance().getLogger().log(Level.SEVERE, "Redistribution failed");
						throw new DistributionException(e);
					}
				}
			} else {
				ServerContext.getInstance().getLogger().log(Level.SEVERE, "Redistribution failed due to empty cluster");
				throw new EmptyClusterException();
			}
		}

	}

	private static void broadcastRequests(String taskName, LinkedList<NodeRequest> requests, int maxBroadcastRetry)
			throws IOException, EmptyClusterException, DistributionException {
		NodeRequest[] requestArray = new NodeRequest[requests.size()];
		int i = 0;
		for (NodeRequest request : requests) {
			requestArray[i] = request;
			i++;
		}
		broadcastRequests(taskName, requestArray, 0, maxBroadcastRetry);
	}

	public static void broadcastRequests(String taskName, NodeRequest[] requests)
			throws EmptyClusterException, DistributionException {
		broadcastRequests(taskName, requests, 0, MAX_RETRY);
	}

	private static void broadcastRequests(String taskName, NodeRequest[] requests, int retry, int maxRetry)
			throws EmptyClusterException, DistributionException {
		List<ClientThread> clientThreads = ServerContext.getInstance().getClientThreads().getList(taskName);
		if (clientThreads.isEmpty()) {
			throw new EmptyClusterException();
		} else {

			Iterator<ClientThread> iterator;
			int i = 0;
			try {
				while (i < requests.length) {
					iterator = clientThreads.iterator();
					while (iterator.hasNext() && i < requests.length) {
						iterator.next().sendRequest(requests[i]);
						i++;
					}
				}
			} catch (IOException e) {
				if (retry < maxRetry) {
					ServerContext.getInstance().getLogger().log(Level.INFO, "Broadcast retry " + retry);
					broadcastRequests(taskName, MathUtil.subArray(requests, i), retry + 1, maxRetry);
				} else {
					throw new DistributionException(e);
				}
			}

		}

	}

	public static void broadcastStopRequests(List<ClientThread> clientThreads, NodeRequest stopRequest)
			throws EmptyClusterException {
		if (clientThreads.isEmpty()) {
			throw new EmptyClusterException();
		} else {

			Iterator<ClientThread> iterator;
			iterator = clientThreads.iterator();
			while (iterator.hasNext()) {
				try {
					iterator.next().sendStopRequest(stopRequest);
				} catch (Exception e) {
					ServerContext.getInstance().getLogger().log(Level.SEVERE, e);
				}
			}
		}

	}

}
