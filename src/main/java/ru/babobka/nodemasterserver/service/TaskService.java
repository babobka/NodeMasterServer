package ru.babobka.nodemasterserver.service;



import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskResult;


public interface TaskService {
	
	TaskResult getResult(Map<String,String> requestArguments, TaskContext taskContext) throws TimeoutException;
	
	TaskResult cancelTask(UUID taskId);

	
}
