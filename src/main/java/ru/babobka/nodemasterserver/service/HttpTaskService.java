package ru.babobka.nodemasterserver.service;



import ru.babobka.subtask.model.SubTask;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;

public interface HttpTaskService {
	
	HttpResponse getResult(HttpRequest httpRequest, SubTask task);
	
	HttpResponse cancelTask(HttpRequest httpRequest);

	
}
