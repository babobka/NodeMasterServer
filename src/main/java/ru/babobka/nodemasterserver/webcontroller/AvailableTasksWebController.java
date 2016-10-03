package ru.babobka.nodemasterserver.webcontroller;

import ru.babobka.nodemasterserver.pool.FactoryPool;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.webcontroller.WebController;

public class AvailableTasksWebController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) {
		return HttpResponse.jsonResponse(FactoryPool.getInstance().getAvailableTasks());
	}

}
