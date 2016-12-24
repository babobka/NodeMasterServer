package ru.babobka.nodemasterserver.webcontroller;

import java.util.UUID;

import org.json.JSONException;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.model.ResponseStorage;
import ru.babobka.nodemasterserver.model.ResponsesArrayMeta;

import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.runnable.WebController;

public class TasksInfoWebController extends WebController {

	private final ResponseStorage responseStorage = Container.getInstance()
			.get(ResponseStorage.class);

	@Override
	public HttpResponse onGet(HttpRequest request) throws JSONException {

		String taskIdString = request.getUrlParam("taskId");
		if (!taskIdString.isEmpty()) {

			UUID taskId = UUID.fromString(taskIdString);
			ResponsesArrayMeta task = responseStorage.getTaskMeta(taskId);
			if (task != null) {
				return HttpResponse.jsonResponse(task);
			} else {
				return HttpResponse.NOT_FOUND_RESPONSE;
			}

		} else {
			return HttpResponse
					.jsonResponse(responseStorage.getRunningTasksMetaMap());
		}
	}

}
