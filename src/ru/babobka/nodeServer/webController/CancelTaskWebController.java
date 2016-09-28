package ru.babobka.nodeServer.webController;

import ru.babobka.nodeServer.service.HttpTaskService;
import ru.babobka.nodeServer.service.HttpTaskServiceImpl;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.webcontroller.WebController;

public class CancelTaskWebController extends WebController {

	private final HttpTaskService httpTaskService = HttpTaskServiceImpl
			.getInstance();

	@Override
	public HttpResponse onDelete(HttpRequest request) {
		return httpTaskService.cancelTask(request);
	}

}
