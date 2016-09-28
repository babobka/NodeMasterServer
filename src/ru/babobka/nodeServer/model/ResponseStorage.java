package ru.babobka.nodeServer.model;

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

	public void put(Long id, ResponsesArray responses) {
		responsesMap.put(id, responses);
	}

	public ResponsesArray get(Long id) {
		return responsesMap.get(id);
	}

	public boolean exists(long taskId) {
		return responsesMap.containsKey(taskId);
	}

	public synchronized void setBadResponse(Long taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.add(NodeResponse.badResponse(taskId));
		}
	}

	public synchronized void setStopResponse(Long taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.add(NodeResponse.stoppedResponse(taskId));
		}
	}

	public synchronized void setStopAllResponses(Long taskId) {
		ResponsesArray responsesArray = responsesMap.get(taskId);
		if (responsesArray != null) {
			responsesArray.fill(NodeResponse.stoppedResponse(taskId));
		}
	}

	public Map<Long, ResponsesArrayMeta> getRunningTasksMetaMap() {
		Map<Long, ResponsesArrayMeta> taskMap = new HashMap<>();
		for (Map.Entry<Long, ResponsesArray> entry : responsesMap.entrySet()) {
			if (!entry.getValue().isComplete()) {
				taskMap.put(entry.getKey(), entry.getValue().getMeta());
			}
		}
		return taskMap;
	}

	public ResponsesArrayMeta getTaskMeta(Long taskId) {
		return responsesMap.get(taskId).getMeta();
	}

	public void clear(long id) {
		responsesMap.remove(id);
	}

}
