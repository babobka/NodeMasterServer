package ru.babobka.nodeServer.model;

import java.util.Date;
import java.util.Map;

public class ResponsesArrayMeta {

	private String taskName;

	private Map<String, String> params;

	private long startTime;

	ResponsesArrayMeta(String taskName, Map<String, String> params,
			long startTime) {
		this.taskName = taskName;
		this.params = params;
		this.startTime = startTime;
	}

	public String getTaskName() {
		return taskName;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public long getStartTime() {
		return startTime;
	}

	public Date getStartDate() {
		return new Date(startTime);
	}

}
