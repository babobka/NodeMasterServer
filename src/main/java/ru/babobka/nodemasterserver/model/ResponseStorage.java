package ru.babobka.nodemasterserver.model;

import ru.babobka.nodeserials.NodeResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dolgopolov.a on 03.08.15.
 */
public class ResponseStorage {

	private final Map<Long, ResponsesArray> responsesMap;

	public ResponseStorage() {
		responsesMap = new ConcurrentHashMap<>();
	}

	public void put(long taskId, ResponsesArray responses) {
		responsesMap.put(taskId, responses);
	}

	public ResponsesArray get(long taskId) {

		return responsesMap.get(taskId);
	}

	public boolean exists(long taskId) {
		return responsesMap.containsKey(taskId);
	}

	public void addBadResponse(long taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.add(NodeResponse.badResponse(taskId));
		}
	}

	public void addStopResponse(long taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.add(NodeResponse.stoppedResponse(taskId));
		}
	}

	public void setStopAllResponses(long taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.fill(NodeResponse.stoppedResponse(taskId));
		}
	}

	public synchronized Map<Long, ResponsesArrayMeta> getRunningTasksMetaMap() {
		Map<Long, ResponsesArrayMeta> taskMap = new HashMap<>();
		for (Map.Entry<Long, ResponsesArray> entry : responsesMap.entrySet()) {
			if (!entry.getValue().isComplete()) {
				taskMap.put(entry.getKey(), entry.getValue().getMeta());
			}
		}
		return taskMap;
	}

	public ResponsesArrayMeta getTaskMeta(long taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			return responsesArray.getMeta();
		}
		return null;
	}

	public synchronized void clear(long taskId) {
		responsesMap.remove(taskId);
	}

}
