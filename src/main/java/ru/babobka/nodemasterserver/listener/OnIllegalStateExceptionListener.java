package ru.babobka.nodemasterserver.listener;

import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.model.HttpResponse;

public class OnIllegalStateExceptionListener implements OnExceptionListener {

	@Override
	public HttpResponse onException(Exception e) {
		return HttpResponse.textResponse(e.getMessage(), HttpResponse.ResponseCode.BAD_REQUEST);
	}

}
