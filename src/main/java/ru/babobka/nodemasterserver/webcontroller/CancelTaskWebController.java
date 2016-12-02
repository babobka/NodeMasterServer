package ru.babobka.nodemasterserver.webcontroller;

import ru.babobka.nodemasterserver.service.TaskService;
import ru.babobka.nodemasterserver.service.TaskServiceImpl;
import ru.babobka.nodemasterserver.util.TextUtil;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.runnable.WebController;

public class CancelTaskWebController extends WebController {

	private final TaskService taskService = TaskServiceImpl.getInstance();

	private static final long NON_VALID_TASK_ID = -1;

	@Override
	public HttpResponse onDelete(HttpRequest request) {
		long taskId = TextUtil.tryParseLong(request.getParam("taskId"), NON_VALID_TASK_ID);
		if (taskId == NON_VALID_TASK_ID) {
			return HttpResponse.textResponse("Invalid 'taskId'", ResponseCode.BAD_REQUEST);
		}
		return HttpResponse.jsonResponse(taskService.cancelTask(taskId));
	}

}
