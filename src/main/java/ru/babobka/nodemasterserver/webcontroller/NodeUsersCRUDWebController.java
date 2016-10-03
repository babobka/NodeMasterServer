package ru.babobka.nodemasterserver.webcontroller;

import org.json.JSONException;
import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.InvalidUserException;
import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodemasterserver.model.UserHttpEntity;
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
				return HttpResponse.textResponse(ResponseCode.BAD_REQUEST, ResponseCode.BAD_REQUEST);
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
		UserHttpEntity userHttpEntity = new UserHttpEntity(new JSONObject(request.getBody()));

		if (userHttpEntity.getTaskCount() != null && userHttpEntity.getTaskCount() < 0) {
			return HttpResponse.textResponse("'taskCount' is negative", ResponseCode.BAD_REQUEST);
		}
		if (userHttpEntity.getEmail() != null && !userHttpEntity.getEmail().matches(User.EMAIL_PATTERN)) {
			return HttpResponse.textResponse("'email' is not valid", ResponseCode.BAD_REQUEST);
		}
		if (nodeUsersService.update(userName, userHttpEntity)) {
			return HttpResponse.ok();
		} else {
			return HttpResponse.textResponse(ResponseCode.INTERNAL_SERVER_ERROR.toString(),
					ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}
}
