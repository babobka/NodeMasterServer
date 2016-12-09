package ru.babobka.nodemasterserver.listener;

import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.model.HttpResponse;

public class OnTimeoutExceptionListener implements OnExceptionListener {

	@Override
	public HttpResponse onException(Exception e) {
		return HttpResponse.textResponse("Request timeout", HttpResponse.ResponseCode.REQUEST_TIMEOUT);
	}
}
