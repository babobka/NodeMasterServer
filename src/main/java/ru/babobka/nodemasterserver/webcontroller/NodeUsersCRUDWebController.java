package ru.babobka.nodemasterserver.webcontroller;

import org.json.JSONException;
import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.InvalidUserException;
import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodemasterserver.service.NodeUsersService;
import ru.babobka.nodemasterserver.service.NodeUsersServiceImpl;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.webcontroller.WebController;

public class NodeUsersCRUDWebController extends WebController {

	private final NodeUsersService nodeUsersService = NodeUsersServiceImpl.getInstance();

	@Override
	public HttpResponse onGet(HttpRequest request) throws JSONException {
		String userName = request.getUrlParam("userName");
		if (!userName.isEmpty()) {
			User user = nodeUsersService.get(userName);
			if (user == null) {
				return HttpResponse.NOT_FOUND_RESPONSE;
			} else {
				return HttpResponse.jsonResponse(user);
			}
		} else {
			return HttpResponse.jsonResponse(nodeUsersService.getList());
		}
	}

	@Override
	public HttpResponse onDelete(HttpRequest request) {

		String userName = request.getUrlParam("userName");
		if (userName == null) {
			return HttpResponse.textResponse("Parameter 'userName' was not set", ResponseCode.BAD_REQUEST);
		} else {
			if (nodeUsersService.remove(userName)) {
				return HttpResponse.ok();
			} else {
				return HttpResponse.textResponse(ResponseCode.INTERNAL_SERVER_ERROR,
						ResponseCode.INTERNAL_SERVER_ERROR);
			}
		}

	}

	@Override
	public HttpResponse onPatch(HttpRequest request) {

		try {
			User user = User.fromJson(new JSONObject(request.getBody()));
			if (nodeUsersService.add(user)) {
				return HttpResponse.ok();
			} else {
				return HttpResponse.textResponse(ResponseCode.BAD_REQUEST.toString(), ResponseCode.BAD_REQUEST);
			}
		} catch (InvalidUserException e) {
			return HttpResponse.exceptionResponse(e, ResponseCode.BAD_REQUEST);
		}

	}

	@Override
	public HttpResponse onPost(HttpRequest request) throws JSONException {
		String userName = request.getUrlParam("name");
		JSONObject userJsonObject = new JSONObject(request.getBody());
		Integer taskCount = null;
		String email = null;
		String password = null;
		if (userJsonObject.isNull("name")) {
			return HttpResponse.textResponse("'name' must be set", ResponseCode.BAD_REQUEST);
		}
		if (!userJsonObject.isNull("taskCount")) {
			taskCount = userJsonObject.getInt("taskCount");
			if (taskCount < 0) {
				return HttpResponse.textResponse("'taskCount' is negative", ResponseCode.BAD_REQUEST);
			}
		}
		if (!userJsonObject.isNull("email") && !userJsonObject.getString("email").matches(User.EMAIL_PATTERN)) {
			return HttpResponse.textResponse("'email' is not valid", ResponseCode.BAD_REQUEST);
		}
		if (!userJsonObject.isNull("password")) {
			password = userJsonObject.getString("password");
		}
		if (nodeUsersService.update(userName, userJsonObject.getString("name"), password, email, taskCount)) {
			return HttpResponse.ok();
		} else {
			return HttpResponse.textResponse(ResponseCode.INTERNAL_SERVER_ERROR.toString(),
					ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}
}
