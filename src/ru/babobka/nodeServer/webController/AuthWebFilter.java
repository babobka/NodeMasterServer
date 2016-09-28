package ru.babobka.nodeServer.webController;

import ru.babobka.nodeServer.Server;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.webcontroller.WebFilter;

public class AuthWebFilter implements WebFilter {

	@Override
	public void afterFilter(HttpRequest request, HttpResponse response) {
		// Nothing to do after
	}

	@Override
	public HttpResponse onFilter(HttpRequest request) {
		String login = request.getHeader("X-Login");
		String password = request.getHeader("X-Password");
		if (!login.equals(Server.getConfigData().getRestServiceLogin())
				|| !password.equals(Server.getConfigData().getRestServicePassword())) {
			return HttpResponse.textResponse("Bad login/password combination", ResponseCode.UNAUTHORIZED);
		} else {
			return null;
		}
	}

}
