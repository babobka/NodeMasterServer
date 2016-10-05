package ru.babobka.nodemasterserver.service;



import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;

public interface HttpTaskService {
	
	HttpResponse getResult(HttpRequest httpRequest, TaskContext taskContext);
	
	HttpResponse cancelTask(HttpRequest httpRequest);

	
}
