package ru.babobka.nodeServer.webController;

import org.json.JSONException;
import org.json.JSONObject;

import ru.babobka.nodeServer.Server;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.webcontroller.WebController;

public class GetStartTimeWebController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.append("startTime", Server.START_TIME);
		return HttpResponse.jsonResponse(jsonObject);
	}

}
