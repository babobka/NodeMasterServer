package ru.babobka.nodemasterserver.webcontroller;

import java.util.UUID;

import ru.babobka.nodemasterserver.service.TaskService;
import ru.babobka.nodemasterserver.service.TaskServiceImpl;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.runnable.WebController;

public class CancelTaskWebController extends WebController {

	private final TaskService taskService = TaskServiceImpl.getInstance();

	private static final String UUID_REGULAR = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

	@Override
	public HttpResponse onDelete(HttpRequest request) {
		String taskIdParam = request.getParam("taskId");
		if (!taskIdParam.matches(UUID_REGULAR)) {
			return HttpResponse.textResponse("Invalid 'taskId'", ResponseCode.BAD_REQUEST);
		}
		UUID taskId = UUID.fromString(taskIdParam);
		return HttpResponse.jsonResponse(taskService.cancelTask(taskId));
	}

}
