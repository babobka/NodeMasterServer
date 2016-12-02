package ru.babobka.nodemasterserver.webcontroller;


import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.runnable.WebController;

public class ClusterInfoWebController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) {
		return HttpResponse.jsonResponse(MasterServerContext.getInstance().getSlaves().getCurrentClusterUserList());
	}

}
