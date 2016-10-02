package ru.babobka.nodemasterserver.webController;

import ru.babobka.nodemasterserver.service.HttpTaskService;
import ru.babobka.nodemasterserver.service.HttpTaskServiceImpl;
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
