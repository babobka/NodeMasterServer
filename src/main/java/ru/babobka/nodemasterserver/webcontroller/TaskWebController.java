package ru.babobka.nodemasterserver.webcontroller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.TimeoutException;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.service.TaskService;
import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.runnable.WebController;

public class TaskWebController extends WebController {

	private TaskService taskService = Container.getInstance()
			.get(TaskService.class);

	private TaskPool taskPool = Container.getInstance().get(TaskPool.class);

	@Override
	public HttpResponse onGet(HttpRequest request)
			throws IOException, TimeoutException {
		String taskName = request.getUri().replaceFirst("/", "");
		taskName = taskName.substring(taskName.indexOf('/') + 1,
				taskName.indexOf('?'));
		TaskContext taskContext = taskPool
				.get(URLDecoder.decode(taskName, "UTF-8"));
		return HttpResponse.jsonResponse(
				taskService.getResult(request.getUrlParams(), taskContext));
	}

	@Override
	public HttpResponse onHead(HttpRequest request) {
		return HttpResponse.ok();
	}

}
