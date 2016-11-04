package ru.babobka.nodemasterserver.webcontroller;

import java.io.IOException;
import java.net.URLDecoder;

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
		taskName = taskName.substring(taskName.indexOf('/') + 1, taskName.indexOf('?'));
		TaskContext taskContext = taskPool.get(URLDecoder.decode(taskName, "UTF-8"));
		return HttpResponse.jsonResponse(taskService.getResult(request, taskContext));
	}

	@Override
	public HttpResponse onHead(HttpRequest request) {
		return HttpResponse.ok();
	}

}
