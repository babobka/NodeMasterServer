package ru.babobka.nodemasterserver.webcontroller;

import java.io.IOException;

import ru.babobka.nodemasterserver.service.TaskService;
import ru.babobka.nodemasterserver.service.TaskServiceImpl;
import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.runnable.WebController;

public class TaskWebController extends WebController {

	private TaskService taskService = TaskServiceImpl.getInstance();

	private TaskPool taskPool = TaskPool.getInstance();

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException {
		String taskName = request.getUri().replaceFirst("/", "");
		TaskContext taskContext = taskPool.get(taskName);
		return HttpResponse.jsonResponse(taskService.getResult(request, taskContext));
	}

	@Override
	public HttpResponse onHead(HttpRequest request) {
		return HttpResponse.ok();
	}

}
