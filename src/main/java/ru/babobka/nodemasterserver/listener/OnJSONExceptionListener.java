package ru.babobka.nodemasterserver.listener;

import org.json.JSONException;

import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.model.HttpResponse;

public class OnJSONExceptionListener implements OnExceptionListener {

	@Override
	public HttpResponse onException(Exception e) {
		if (e instanceof JSONException) {
			return HttpResponse.textResponse("Invalid JSON input", HttpResponse.ResponseCode.BAD_REQUEST);
		}
		return null;
	}

}
