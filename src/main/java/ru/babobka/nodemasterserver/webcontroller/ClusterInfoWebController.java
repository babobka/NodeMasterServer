package ru.babobka.nodemasterserver.webcontroller;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.runnable.WebController;

public class ClusterInfoWebController extends WebController {

	private final Slaves slaves = Container.getInstance().get(Slaves.class);

	@Override
	public HttpResponse onGet(HttpRequest request) {
		return HttpResponse.jsonResponse(slaves.getCurrentClusterUserList());
	}

}
