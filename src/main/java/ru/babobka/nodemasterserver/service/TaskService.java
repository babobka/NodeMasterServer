package ru.babobka.nodemasterserver.service;



import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskResult;
import ru.babobka.vsjws.model.HttpRequest;

public interface TaskService {
	
	TaskResult getResult(HttpRequest httpRequest, TaskContext taskContext);
	
	TaskResult cancelTask(HttpRequest httpRequest);

	
}
