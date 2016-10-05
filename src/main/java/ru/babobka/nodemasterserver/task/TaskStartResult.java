package ru.babobka.nodemasterserver.task;

public class TaskStartResult {

	private final long taskId;

	private final boolean failed;

	private final boolean systemError;

	private final String message;

	public TaskStartResult(long taskId, boolean failed, boolean systemError, String message) {
		this.taskId = taskId;
		this.failed = failed;
		this.message = message;
		this.systemError = systemError;
	}

	public TaskStartResult(long taskId) {
		this(taskId, false, false, null);
	}

	public boolean isSystemError() {
		return systemError;
	}

	public long getTaskId() {
		return taskId;
	}

	public boolean isFailed() {
		return failed;
	}

	public String getMessage() {
		return message;
	}

}
