package ru.babobka.nodemasterserver.webcontroller;


import ru.babobka.nodemasterserver.server.ServerContext;
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
		if (!login.equals(ServerContext.getInstance().getConfig().getRestServiceLogin())
				|| !password.equals(ServerContext.getInstance().getConfig().getRestServicePassword())) {
			return HttpResponse.textResponse("Bad login/password combination", ResponseCode.UNAUTHORIZED);
		} else {
			return null;
		}
	}

}
