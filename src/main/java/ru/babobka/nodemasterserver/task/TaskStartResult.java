package ru.babobka.nodemasterserver.task;

import java.util.UUID;

public class TaskStartResult {

	private final UUID taskId;

	private final boolean failed;

	private final boolean systemError;

	private final String message;

	public TaskStartResult(UUID taskId, boolean failed, boolean systemError, String message) {
		this.taskId = taskId;
		this.failed = failed;
		this.message = message;
		this.systemError = systemError;
	}

	public TaskStartResult(UUID taskId) {
		this(taskId, false, false, null);
	}

	public boolean isSystemError() {
		return systemError;
	}

	public UUID getTaskId() {
		return taskId;
	}

	public boolean isFailed() {
		return failed;
	}

	public String getMessage() {
		return message;
	}

}
