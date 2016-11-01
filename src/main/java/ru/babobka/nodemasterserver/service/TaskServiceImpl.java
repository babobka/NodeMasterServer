package ru.babobka.nodemasterserver.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ru.babobka.nodemasterserver.exception.DistributionException;
import ru.babobka.nodemasterserver.exception.EmptyClusterException;
import ru.babobka.nodemasterserver.model.ResponseStorage;
import ru.babobka.nodemasterserver.model.ResponsesArray;
import ru.babobka.nodemasterserver.model.Timer;
import ru.babobka.nodemasterserver.server.ServerContext;
import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.nodemasterserver.task.TaskResult;
import ru.babobka.nodemasterserver.task.TaskStartResult;
import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodemasterserver.util.DistributionUtil;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.subtask.model.RequestDistributor;
import ru.babobka.subtask.model.SubTask;
import ru.babobka.vsjws.model.HttpRequest;


public class TaskServiceImpl implements TaskService {

	private static TaskPool taskPool = TaskPool.getInstance();

	private static final String WRONG_ARGUMENTS = "Wrong arguments";

	private static volatile TaskServiceImpl instance;

	private TaskServiceImpl() {

	}

	public static TaskServiceImpl getInstance() {
		TaskServiceImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (TaskServiceImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new TaskServiceImpl();
				}
			}
		}
		return localInstance;
	}

	private String getTaskName(HttpRequest request) {

		return request.getUri().replaceFirst("/", "");

	}

	private void startTask(TaskContext taskContext, long taskId, Map<String, String> arguments)
			throws EmptyClusterException, DistributionException {
		int currentClusterSize;
		String taskName = taskContext.getConfig().getName();
		if (isRequestDataIsTooSmall(taskContext.getTask(), arguments)) {
			currentClusterSize = 1;
		} else {
			currentClusterSize = ServerContext.getInstance().getSlaves().getClusterSize(taskName);
		}
		if (currentClusterSize < 1) {
			throw new EmptyClusterException();
		} else {
			ServerContext.getInstance().getResponseStorage().put(taskId,
					new ResponsesArray(currentClusterSize, taskContext, arguments));
			NodeRequest[] requests = taskContext.getTask().getDistributor().distribute(arguments, currentClusterSize,
					taskId);
			DistributionUtil.broadcastRequests(taskName, requests);
		}

	}

	private boolean isRequestDataIsTooSmall(SubTask task, Map<String, String> arguments) {
		NodeRequest request = task.getDistributor().distribute(arguments, 1, 0)[0];
		if (task.isRequestDataTooSmall(request.getAddition())) {
			return true;
		}
		return false;

	}

	private TaskStartResult startTask(HttpRequest request, TaskContext taskContext, long taskId) {

		RequestDistributor requestDistributor = taskContext.getTask().getDistributor();
		if (requestDistributor.isValidArguments(request.getUrlParams())) {
			ServerContext.getInstance().getLogger().log("Task id is " + taskId);
			try {
				startTask(taskContext, taskId, request.getUrlParams());
				return new TaskStartResult(taskId);
			} catch (DistributionException e) {
				ServerContext.getInstance().getLogger().log(Level.SEVERE, e);
				try {
					DistributionUtil.broadcastStopRequests(
							ServerContext.getInstance().getSlaves().getListByTaskId(taskId),
							new NodeRequest(taskId, true, getTaskName(request)));
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
				return taskPool.get(responsesArray.getMeta().getTaskName()).getTask().getReducer()
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
	public TaskResult getResult(HttpRequest request, TaskContext taskContext) {

		Map<String, Serializable> resultMap;

		Long taskId = (long) (Math.random() * Integer.MAX_VALUE);
		try {
			TaskStartResult startResult = startTask(request, taskContext, taskId);
			if (!startResult.isFailed()) {
				Timer timer = new Timer();
				resultMap = getTaskResult(startResult.getTaskId());
				if (resultMap != null) {
					return new TaskResult(timer.getTimePassed(), resultMap);
				} else {
					throw new IllegalStateException("Can not find result");
				}
			} else if (startResult.isSystemError()) {
				throw new IllegalStateException("System error");
			} else {
				throw new IllegalArgumentException(startResult.getMessage());
			}
		} finally {
			ServerContext.getInstance().getResponseStorage().clear(taskId);
		}

	}

	@Override
	public TaskResult cancelTask(HttpRequest httpRequest) {
		try {
			long taskId = Long.parseLong(httpRequest.getParam("taskId"));
			List<SlaveThread> clientThreads = ServerContext.getInstance().getSlaves().getListByTaskId(taskId);
			ServerContext.getInstance().getLogger().log("Trying to cancel task " + taskId);
			ResponsesArray responsesArray = ServerContext.getInstance().getResponseStorage().get(taskId);
			if (responsesArray != null) {
				DistributionUtil.broadcastStopRequests(clientThreads,
						new NodeRequest(taskId, true, responsesArray.getMeta().getTaskName()));
				ServerContext.getInstance().getResponseStorage().setStopAllResponses(taskId);
				return new TaskResult("Task " + taskId + " was canceled");
			} else {
				ServerContext.getInstance().getLogger().log(Level.SEVERE, "No task was found for given task id");
				throw new IllegalArgumentException("No task was found for given task id");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(WRONG_ARGUMENTS, e);
		} catch (EmptyClusterException e) {
			ServerContext.getInstance().getLogger().log(Level.SEVERE, e);
			throw new IllegalStateException("Can not cancel task due to empty cluster", e);
		}
	}

}
