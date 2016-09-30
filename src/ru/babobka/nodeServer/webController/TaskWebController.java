package ru.babobka.nodeServer.webController;

import java.io.IOException;

import ru.babobka.nodeServer.pool.FactoryPool;
import ru.babobka.nodeServer.service.HttpTaskService;
import ru.babobka.nodeServer.service.HttpTaskServiceImpl;
import ru.babobka.subtask.model.SubTask;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.webcontroller.WebController;

public class TaskWebController extends WebController {

	private HttpTaskService httpTaskService = HttpTaskServiceImpl.getInstance();

	private FactoryPool factoryPool = FactoryPool.getInstance();

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException {
		String taskName = request.getUri().replaceFirst("/", "");
		SubTask task = factoryPool.get(taskName);
		return httpTaskService.getResult(request, task);
	}

	@Override
	public HttpResponse onHead(HttpRequest request) {
		return HttpResponse.ok();
	}

}
