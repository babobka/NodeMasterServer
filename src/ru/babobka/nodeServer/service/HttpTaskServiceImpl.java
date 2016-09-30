package ru.babobka.nodeServer.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ru.babobka.nodeServer.exception.DistributionException;
import ru.babobka.nodeServer.exception.EmptyClusterException;
import ru.babobka.nodeServer.model.ResponseStorage;
import ru.babobka.nodeServer.model.ResponsesArray;
import ru.babobka.nodeServer.model.ServerContext;
import ru.babobka.nodeServer.model.TaskResult;
import ru.babobka.nodeServer.model.TaskStartResult;
import ru.babobka.nodeServer.model.Timer;
import ru.babobka.nodeServer.pool.FactoryPool;
import ru.babobka.nodeServer.thread.ClientThread;
import ru.babobka.nodeServer.util.DistributionUtil;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.subtask.model.RequestDistributor;
import ru.babobka.subtask.model.SubTask;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;

public class HttpTaskServiceImpl implements HttpTaskService {

	private static FactoryPool factoryPool = FactoryPool.getInstance();

	private static final String WRONG_ARGUMENTS = "Wrong arguments";

	private static volatile HttpTaskServiceImpl instance;

	private HttpTaskServiceImpl() {

	}

	public static HttpTaskServiceImpl getInstance() {
		HttpTaskServiceImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (HttpTaskServiceImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new HttpTaskServiceImpl();
				}
			}
		}
		return localInstance;
	}

	private String getTaskName(HttpRequest request) {

		return request.getUri().replaceFirst("/", "");

	}

	private void startTask(SubTask task, long taskId, Map<String, String> arguments)
			throws EmptyClusterException, DistributionException {
		int currentClusterSize;
		if (isRequestDataIsTooSmall(task, arguments)) {
			currentClusterSize = 1;
		} else {
			currentClusterSize = ServerContext.getInstance().getClientThreads().getClusterSize(task.getTaskName());
		}
		if (currentClusterSize < 1) {
			throw new EmptyClusterException();
		} else {
			ServerContext.getInstance().getResponseStorage().put(taskId,
					new ResponsesArray(currentClusterSize, task.getTaskName(), task, arguments));
			NodeRequest[] requests = task.getDistributor().distribute(arguments, currentClusterSize, taskId);
			DistributionUtil.broadcastRequests(task.getTaskName(), requests, 0, ServerContext.getInstance().getConfig().getMaxBroadcastRetry());
		}

	}

	private boolean isRequestDataIsTooSmall(SubTask task, Map<String, String> arguments) {
		NodeRequest request = task.getDistributor().distribute(arguments, 1, 0)[0];
		if (task.isRequestDataTooSmall(request.getAddition())) {
			return true;
		}
		return false;

	}

	private TaskStartResult startTask(HttpRequest request, SubTask task, long taskId) {
		String requestUri = getTaskName(request);
		RequestDistributor requestDistributor = task.getDistributor();
		if (requestDistributor.isValidArguments(request.getUrlParams())) {
			ServerContext.getInstance().getLogger().log(Level.INFO, "Task id is " + taskId);
			try {
				startTask(task, taskId, request.getUrlParams());
				return new TaskStartResult(taskId);
			} catch (DistributionException e) {
				ServerContext.getInstance().getLogger().log(Level.SEVERE, e);
				try {
					DistributionUtil.broadcastStopRequests(ServerContext.getInstance().getClientThreads().getListByTaskId(taskId),
							new NodeRequest(taskId, true, requestUri));
				} catch (EmptyClusterException e1) {
					ServerContext.getInstance().getLogger().log(e1);
				}
				return new TaskStartResult(taskId, true, true, "Can not distribute your request");
			} catch (EmptyClusterException e) {
				ServerContext.getInstance().getLogger().log(e);
				return new TaskStartResult(taskId, true, true, "Can not distribute due to empty cluster");
			}

		} else {
			ServerContext.getInstance().getLogger().log(Level.SEVERE, WRONG_ARGUMENTS);
			return new TaskStartResult(taskId, true, false, WRONG_ARGUMENTS);
		}

	}

	private Map<String, Serializable> getTaskResult(long taskId) {
		try {
			ResponseStorage responseStorage = ServerContext.getInstance().getResponseStorage();
			ResponsesArray responsesArray = responseStorage.get(taskId);
			if (responsesArray != null) {
				return factoryPool.get(responsesArray.getMeta().getTaskName()).getReducer()
						.reduce(responsesArray.getResponseList());

			} else {
				ServerContext.getInstance().getLogger().log(Level.SEVERE, "No such task");
			}
		} catch (Exception e) {
			ServerContext.getInstance().getLogger().log(e);
		}
		return null;
	}

	@Override
	public HttpResponse getResult(HttpRequest request, SubTask task) {

		Map<String, Serializable> resultMap;

		Long taskId = (long) (Math.random() * Integer.MAX_VALUE);
		try {
			TaskStartResult startResult = startTask(request, task, taskId);
			if (!startResult.isFailed()) {
				Timer timer = new Timer();
				resultMap = getTaskResult(startResult.getTaskId());
				if (resultMap != null) {
					return HttpResponse.jsonResponse(new TaskResult(timer.getTimePassed(), resultMap));
				} else {
					return HttpResponse.jsonResponse(new TaskResult("Can not find result"),
							ResponseCode.INTERNAL_SERVER_ERROR);
				}
			} else if (startResult.isSystemError()) {
				return HttpResponse.jsonResponse(new TaskResult(startResult.getMessage()),
						ResponseCode.INTERNAL_SERVER_ERROR);
			} else {
				return HttpResponse.jsonResponse(new TaskResult(startResult.getMessage()), ResponseCode.BAD_REQUEST);
			}
		} finally {
			ServerContext.getInstance().getResponseStorage().clear(taskId);
		}

	}

	@Override
	public HttpResponse cancelTask(HttpRequest httpRequest) {
		try {
			long taskId = Long.parseLong(httpRequest.getParam("taskId"));
			List<ClientThread> clientThreads = ServerContext.getInstance().getClientThreads().getListByTaskId(taskId);
			ServerContext.getInstance().getLogger().log(Level.INFO, "Trying to cancel task " + taskId);
			ResponsesArray responsesArray = ServerContext.getInstance().getResponseStorage().get(taskId);
			if (responsesArray != null) {
				DistributionUtil.broadcastStopRequests(clientThreads,
						new NodeRequest(taskId, true, responsesArray.getMeta().getTaskName()));
				ServerContext.getInstance().getResponseStorage().setStopAllResponses(taskId);
				return HttpResponse.jsonResponse(new TaskResult("Task " + taskId + " was canceled"));
			} else {
				ServerContext.getInstance().getLogger().log(Level.SEVERE, "No task was found for given task id");
				return HttpResponse.jsonResponse(new TaskResult("No task was found for given task id"),
						ResponseCode.BAD_REQUEST);
			}
		} catch (NumberFormatException e) {
			ServerContext.getInstance().getLogger().log(Level.SEVERE, WRONG_ARGUMENTS);
			return HttpResponse.jsonResponse(new TaskResult(WRONG_ARGUMENTS), ResponseCode.BAD_REQUEST);
		} catch (EmptyClusterException e) {
			ServerContext.getInstance().getLogger().log(Level.SEVERE, e);
			return HttpResponse.jsonResponse(new TaskResult("Can not cancel task due to empty cluster"),
					ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}

}
