package ru.babobka.nodemasterserver.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.exception.DistributionException;
import ru.babobka.nodemasterserver.exception.EmptyClusterException;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodemasterserver.util.MathUtil;
import ru.babobka.nodeserials.NodeRequest;

public final class DistributionService {

	private static final int MAX_RETRY = 5;

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	private final Slaves slaves = Container.getInstance().get(Slaves.class);

	public void redistribute(SlaveThread slaveThread)
			throws DistributionException, EmptyClusterException {

		if (slaveThread.getRequestMap().size() > 0) {
			if (!slaves.isEmpty()) {
				logger.log("Redistribution");
				Map<String, LinkedList<NodeRequest>> requestsByUri = slaveThread
						.getRequestsGroupedByTask();
				for (Map.Entry<String, LinkedList<NodeRequest>> requestByUriEntry : requestsByUri
						.entrySet()) {
					try {
						broadcastRequests(requestByUriEntry.getKey(),
								requestByUriEntry.getValue(), MAX_RETRY);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Redistribution failed");
						throw new DistributionException(e);
					}
				}
			} else {
				logger.log(Level.SEVERE,
						"Redistribution failed due to empty cluster");
				throw new EmptyClusterException();
			}
		}

	}

	private void broadcastRequests(String taskName,
			LinkedList<NodeRequest> requests, int maxBroadcastRetry)
			throws IOException, EmptyClusterException, DistributionException {
		NodeRequest[] requestArray = new NodeRequest[requests.size()];
		int i = 0;
		for (NodeRequest request : requests) {
			requestArray[i] = request;
			i++;
		}
		broadcastRequests(taskName, requestArray, 0, maxBroadcastRetry);
	}

	public void broadcastRequests(String taskName, NodeRequest[] requests)
			throws EmptyClusterException, DistributionException {

		broadcastRequests(taskName, requests, 0, MAX_RETRY);
	}

	private void broadcastRequests(String taskName, NodeRequest[] requests,
			int retry, int maxRetry)
			throws EmptyClusterException, DistributionException {
		List<SlaveThread> clientThreads = slaves.getList(taskName);
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
					logger.log("Broadcast retry " + retry);
					broadcastRequests(taskName, MathUtil.subArray(requests, i),
							retry + 1, maxRetry);
				} else {
					throw new DistributionException(e);
				}
			}

		}

	}

	public void broadcastStopRequests(List<SlaveThread> slaveThreads,
			NodeRequest stopRequest) throws EmptyClusterException {
		if (slaveThreads.isEmpty()) {
			throw new EmptyClusterException();
		} else {
			for (SlaveThread slaveThread : slaveThreads) {
				try {
					slaveThread.sendStopRequest(stopRequest);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e);
				}
			}

		}

	}

}
