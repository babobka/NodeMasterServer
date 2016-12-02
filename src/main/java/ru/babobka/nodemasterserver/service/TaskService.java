package ru.babobka.nodemasterserver.service;



import java.util.Map;

import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskResult;


public interface TaskService {
	
	TaskResult getResult(Map<String,String> requestArguments, TaskContext taskContext);
	
	TaskResult cancelTask(long taskId);

	
}
