package ru.babobka.nodemasterserver.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ru.babobka.nodemasterserver.exception.DistributionException;
import ru.babobka.nodemasterserver.exception.EmptyClusterException;
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodeserials.NodeRequest;

public final class DistributionUtil {

	private static final int MAX_RETRY = 5;

	private DistributionUtil() {

	}

	public static void redistribute(SlaveThread slaveThread) throws DistributionException, EmptyClusterException {

		if (slaveThread.getRequestMap().size() > 0) {
			if (!MasterServerContext.getInstance().getSlaves().isEmpty()) {
				MasterServerContext.getInstance().getLogger().log("Redistribution");
				Map<String, LinkedList<NodeRequest>> requestsByUri = slaveThread.getRequestsGroupedByTask();
				for (Map.Entry<String, LinkedList<NodeRequest>> requestByUriEntry : requestsByUri.entrySet()) {
					try {
						broadcastRequests(requestByUriEntry.getKey(), requestByUriEntry.getValue(), MAX_RETRY);
					} catch (Exception e) {
						MasterServerContext.getInstance().getLogger().log(Level.SEVERE, "Redistribution failed");
						throw new DistributionException(e);
					}
				}
			} else {
				MasterServerContext.getInstance().getLogger().log(Level.SEVERE,
						"Redistribution failed due to empty cluster");
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
		List<SlaveThread> clientThreads = MasterServerContext.getInstance().getSlaves().getList(taskName);
		if (clientThreads.isEmpty()) {
			throw new EmptyClusterException();
		} else {

			Iterator<SlaveThread> iterator;
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
					MasterServerContext.getInstance().getLogger().log("Broadcast retry " + retry);
					broadcastRequests(taskName, MathUtil.subArray(requests, i), retry + 1, maxRetry);
				} else {
					throw new DistributionException(e);
				}
			}

		}

	}

	public static void broadcastStopRequests(List<SlaveThread> slaveThreads, NodeRequest stopRequest)
			throws EmptyClusterException {
		if (slaveThreads.isEmpty()) {
			throw new EmptyClusterException();
		} else {
			for (SlaveThread slaveThread : slaveThreads) {
				try {
					slaveThread.sendStopRequest(stopRequest);
				} catch (Exception e) {
					MasterServerContext.getInstance().getLogger().log(Level.SEVERE, e);
				}
			}

		}

	}

}
