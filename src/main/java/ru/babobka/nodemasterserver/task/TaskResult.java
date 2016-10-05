package ru.babobka.nodemasterserver.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TaskResult {

	private final Long timeTakes;

	private final Map<String, Serializable> resultMap = new HashMap<>();

	private final String message;

	private TaskResult(Long timeTakes, Map<String, Serializable> resultMap, String message) {
		super();
		this.timeTakes = timeTakes;
		if (resultMap != null) {
			this.resultMap.putAll(resultMap);
		}
		this.message = message;
	}

	public TaskResult(String message) {
		this(null, null, message);
	}

	public TaskResult(long timeTakes, Map<String, Serializable> resultMap) {
		this(timeTakes, resultMap, null);
	}

	public long getTimeTakes() {
		return timeTakes;
	}

	public Map<String, Serializable> getResultMap() {
		return resultMap;
	}

	public String getMessage() {
		return message;
	}

}
