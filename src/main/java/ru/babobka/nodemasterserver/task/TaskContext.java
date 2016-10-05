package ru.babobka.nodemasterserver.task;

import ru.babobka.subtask.model.SubTask;

public class TaskContext {

	private final SubTask task;

	private final TaskConfig config;

	public TaskContext(SubTask task, TaskConfig config) {
		super();
		this.task = task;
		this.config = config;
	}

	public SubTask getTask() {
		return task;
	}

	public TaskConfig getConfig() {
		return config;
	}

	public TaskContext newInstance() {
		return new TaskContext(task.newInstance(), config.newInstance());
	}

}
