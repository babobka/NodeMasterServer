package ru.babobka.nodeServer.webController;

import org.json.JSONException;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.model.ResponsesArrayMeta;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;

import ru.babobka.vsjws.webcontroller.WebController;

public class TasksInfoWebController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) throws JSONException {

		String taskIdString = request.getUrlParam("taskId");
		if (!taskIdString.isEmpty()) {
			try {
				Long taskId = Long.parseLong(taskIdString);
				ResponsesArrayMeta task = Server.RESPONSE_STORAGE.getTaskMeta(taskId);
				if (task != null) {
					return HttpResponse.jsonResponse(task);
				} else {
					return HttpResponse.NOT_FOUND_RESPONSE;
				}
			} catch (NumberFormatException e) {
				return HttpResponse.textResponse("'taskId' must be a number", ResponseCode.BAD_REQUEST);
			}

		} else {
			return HttpResponse.jsonResponse(Server.RESPONSE_STORAGE.getRunningTasksMetaMap());
		}
	}

}
