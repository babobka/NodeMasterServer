package ru.babobka.nodemasterserver.listener;

import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.model.HttpResponse;

public class OnTaskNotFoundExceptionListener implements OnExceptionListener {

	@Override
	public HttpResponse onException(Exception e) {
		return HttpResponse.textResponse("Task was not found", HttpResponse.ResponseCode.NOT_FOUND);
	}
}
