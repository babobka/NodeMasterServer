package ru.babobka.nodemasterserver.webcontroller;

import ru.babobka.nodemasterserver.service.TaskService;
import ru.babobka.nodemasterserver.service.TaskServiceImpl;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.runnable.WebController;

public class CancelTaskWebController extends WebController {

	private final TaskService taskService = TaskServiceImpl.getInstance();

	@Override
	public HttpResponse onDelete(HttpRequest request) {
		return HttpResponse.jsonResponse(taskService.cancelTask(request));
	}

}
