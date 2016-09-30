package ru.babobka.nodeServer.webController;


import ru.babobka.nodeServer.model.ServerContext;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.webcontroller.WebController;

public class ClusterInfoWebController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) {
		return HttpResponse.jsonResponse(ServerContext.getInstance().getClientThreads().getCurrentClusterUserList());
	}

}
