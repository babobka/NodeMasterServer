package ru.babobka.nodemasterserver.model;

import ru.babobka.nodeserials.NodeResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dolgopolov.a on 03.08.15.
 */
public class ResponseStorage {

	private final Map<UUID, ResponsesArray> responsesMap;

	public ResponseStorage() {
		responsesMap = new ConcurrentHashMap<>();
	}

	public void put(UUID taskId, ResponsesArray responses) {
		responsesMap.put(taskId, responses);
	}

	public ResponsesArray get(UUID taskId) {

		return responsesMap.get(taskId);
	}

	public boolean exists(UUID taskId) {
		return responsesMap.containsKey(taskId);
	}

	public void addBadResponse(UUID taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.add(NodeResponse.badResponse(taskId));
		}
	}

	public void addStopResponse(UUID taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.add(NodeResponse.stoppedResponse(taskId));
		}
	}

	public void setStopAllResponses(UUID taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.fill(NodeResponse.stoppedResponse(taskId));
		}
	}

	public synchronized Map<UUID, ResponsesArrayMeta> getRunningTasksMetaMap() {
		Map<UUID, ResponsesArrayMeta> taskMap = new HashMap<>();
		for (Map.Entry<UUID, ResponsesArray> entry : responsesMap.entrySet()) {
			if (!entry.getValue().isComplete()) {
				taskMap.put(entry.getKey(), entry.getValue().getMeta());
			}
		}
		return taskMap;
	}

	public ResponsesArrayMeta getTaskMeta(UUID taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			return responsesArray.getMeta();
		}
		return null;
	}

	public synchronized void clear(UUID taskId) {
		responsesMap.remove(taskId);
	}

}
