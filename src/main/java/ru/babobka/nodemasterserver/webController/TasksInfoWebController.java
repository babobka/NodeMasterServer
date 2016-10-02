package ru.babobka.nodemasterserver.webController;

import org.json.JSONException;

import ru.babobka.nodemasterserver.model.ResponsesArrayMeta;
import ru.babobka.nodemasterserver.model.ServerContext;
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
				ResponsesArrayMeta task = ServerContext.getInstance().getResponseStorage().getTaskMeta(taskId);
				if (task != null) {
					return HttpResponse.jsonResponse(task);
				} else {
					return HttpResponse.NOT_FOUND_RESPONSE;
				}
			} catch (NumberFormatException e) {
				return HttpResponse.textResponse("'taskId' must be a number", ResponseCode.BAD_REQUEST);
			}

		} else {
			return HttpResponse.jsonResponse(ServerContext.getInstance().getResponseStorage().getRunningTasksMetaMap());
		}
	}

}
