package ru.babobka.nodemasterserver.model;

import ru.babobka.nodemasterserver.exception.EmptyClusterException;
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodemasterserver.util.DistributionUtil;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Created by dolgopolov.a on 03.08.15.
 */
public final class ResponsesArray {

	private final AtomicInteger size;

	private final int maxSize;

	private final TaskContext taskContext;

	private final ResponsesArrayMeta meta;

	private static final String TASK = "Task";

	private final AtomicReferenceArray<NodeResponse> responseArray;

	public ResponsesArray(int maxSize, TaskContext taskContext, Map<String, String> params) {
		this.maxSize = maxSize;
		this.responseArray = new AtomicReferenceArray<>(maxSize);
		this.taskContext = taskContext;
		this.meta = new ResponsesArrayMeta(taskContext.getConfig().getName(), params, System.currentTimeMillis());
		size = new AtomicInteger(0);
	}

	public synchronized boolean isComplete() {
		for (int i = 0; i < responseArray.length(); i++) {
			if (responseArray.get(i) == null) {
				return false;
			}
		}
		return true;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public synchronized boolean add(NodeResponse response) {

		int corruptedResponseCount = 0;
		if (size.intValue() >= responseArray.length()) {
			return false;
		} else {
			for (int i = 0; i < responseArray.length(); i++) {
				if (responseArray.get(i) == null) {
					responseArray.set(i, response);
					if (response.isStopped()) {
						corruptedResponseCount++;
					}
					size.incrementAndGet();
					if (size.intValue() == responseArray.length()) {
						this.notifyAll();
						if (corruptedResponseCount == size.intValue()) {
							MasterServerContext.getInstance().getLogger()
									.log(TASK + " " + response.getTaskId() + " was canceled");
						} else {
							MasterServerContext.getInstance().getLogger()
									.log(TASK + " " + response.getTaskId() + " is ready ");
						}
					} else if (taskContext.getConfig().isRaceStyle()
							&& taskContext.getTask().getReducer().isValidResponse(response)) {
						List<SlaveThread> clientThreads = MasterServerContext.getInstance().getSlaves()
								.getListByTaskId(response.getTaskId());
						try {
							if (!clientThreads.isEmpty()) {
								MasterServerContext.getInstance().getLogger()
										.log("Cancel all requests for task id " + response.getTaskId());
								DistributionUtil.broadcastStopRequests(clientThreads,
										new NodeRequest(response.getTaskId(), true, response.getTaskName()));
							}
						} catch (EmptyClusterException e) {
							MasterServerContext.getInstance().getLogger().log(e);
						}

					}

					break;

				}
			}
			return true;
		}

	}

	public synchronized void fill(NodeResponse response) {

		if (size.intValue() <= responseArray.length()) {
			for (int i = 0; i < responseArray.length(); i++) {
				if (responseArray.get(i) == null) {
					responseArray.set(i, response);
					size.incrementAndGet();
					if (size.intValue() == responseArray.length()) {
						this.notifyAll();
						MasterServerContext.getInstance().getLogger()
								.log(TASK + " " + response.getTaskId() + " is ready due to filling");
						break;
					}
				}
			}
		}

	}

	public synchronized List<NodeResponse> getResponseList() {
		LinkedList<NodeResponse> responses = new LinkedList<>();
		try {
			while (size.intValue() != responseArray.length()) {
				this.wait();
			}
			for (int i = 0; i < responseArray.length(); i++) {
				responses.add((NodeResponse) responseArray.get(i));
			}
			return responses;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			MasterServerContext.getInstance().getLogger().log(e);
		}
		return responses;

	}

	public ResponsesArrayMeta getMeta() {
		return meta;
	}

	@Override
	public String toString() {
		return meta.getTaskName();
	}

}