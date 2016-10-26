package ru.babobka.nodemasterserver.webcontroller;

import java.io.IOException;

import ru.babobka.nodemasterserver.service.HttpTaskService;
import ru.babobka.nodemasterserver.service.HttpTaskServiceImpl;
import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.subtask.model.SubTask;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.runnable.WebController;

public class TaskWebController extends WebController {

	private HttpTaskService httpTaskService = HttpTaskServiceImpl.getInstance();

	private TaskPool taskPool = TaskPool.getInstance();

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException {
		String taskName = request.getUri().replaceFirst("/", "");
		TaskContext taskContext = taskPool.get(taskName);
		return httpTaskService.getResult(request, taskContext);
	}

	@Override
	public HttpResponse onHead(HttpRequest request) {
		return HttpResponse.ok();
	}

}
