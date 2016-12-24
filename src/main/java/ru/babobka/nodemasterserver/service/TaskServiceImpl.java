package ru.babobka.nodemasterserver.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.exception.DistributionException;
import ru.babobka.nodemasterserver.exception.EmptyClusterException;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.ResponseStorage;
import ru.babobka.nodemasterserver.model.ResponsesArray;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.model.Timer;
import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.nodemasterserver.task.TaskResult;
import ru.babobka.nodemasterserver.task.TaskStartResult;
import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.subtask.model.RequestDistributor;
import ru.babobka.subtask.model.SubTask;

public class TaskServiceImpl implements TaskService {

	private static TaskPool taskPool = Container.getInstance()
			.get(TaskPool.class);

	private static final String WRONG_ARGUMENTS = "Wrong arguments";

	private final Slaves slaves = Container.getInstance().get(Slaves.class);

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	private final ResponseStorage responseStorage = Container.getInstance()
			.get(ResponseStorage.class);

	private final DistributionService distributionService = Container
			.getInstance().get(DistributionService.class);

	private void startTask(TaskContext taskContext, UUID taskId,
			Map<String, String> arguments)
			throws EmptyClusterException, DistributionException {
		int currentClusterSize;
		String taskName = taskContext.getConfig().getName();
		if (isRequestDataIsTooSmall(taskContext.getTask(), arguments)) {
			currentClusterSize = 1;
		} else {
			currentClusterSize = slaves.getClusterSize(taskName);
		}
		responseStorage.put(taskId,
				new ResponsesArray(currentClusterSize, taskContext, arguments));
		if (currentClusterSize > 0) {
			NodeRequest[] requests = taskContext.getTask().getDistributor()
					.distribute(arguments, currentClusterSize, taskId);
			distributionService.broadcastRequests(taskName, requests);
		} else {
			throw new EmptyClusterException();
		}

	}

	private boolean isRequestDataIsTooSmall(SubTask task,
			Map<String, String> arguments) {
		NodeRequest request = task.getDistributor().distribute(arguments, 1,
				UUID.randomUUID())[0];

		if (task.isRequestDataTooSmall(request)) {
			return true;
		}
		return false;

	}

	private TaskStartResult startTask(Map<String, String> requestArguments,
			TaskContext taskContext, UUID taskId) {

		RequestDistributor requestDistributor = taskContext.getTask()
				.getDistributor();
		if (requestDistributor.isValidArguments(requestArguments)) {
			logger.log("Task id is " + taskId);
			try {
				startTask(taskContext, taskId, requestArguments);
				return new TaskStartResult(taskId);
			} catch (DistributionException e) {
				logger.log(Level.SEVERE, e);
				try {
					distributionService.broadcastStopRequests(
							slaves.getListByTaskId(taskId),
							new NodeRequest(taskId, true,
									taskContext.getConfig().getName()));
				} catch (EmptyClusterException e1) {
					logger.log(e1);
				}
				return new TaskStartResult(taskId, true, true,
						"Can not distribute your request");
			} catch (EmptyClusterException e) {
				logger.log(e);
				return new TaskStartResult(taskId, true, true,
						"Can not distribute due to empty cluster");
			}

		} else {
			logger.log(Level.SEVERE, WRONG_ARGUMENTS);
			return new TaskStartResult(taskId, true, false, WRONG_ARGUMENTS);
		}

	}

	private Map<String, Serializable> getTaskResult(UUID taskId)
			throws TimeoutException {
		try {
			ResponsesArray responsesArray = responseStorage.get(taskId);
			if (responsesArray != null) {
				return taskPool.get(responsesArray.getMeta().getTaskName())
						.getTask().getReducer()
						.reduce(responsesArray.getResponseList());

			} else {
				logger.log(Level.SEVERE, "No such task");
			}
		} catch (TimeoutException e) {
			logger.log(Level.WARNING, "getTaskResult() reaches timeout");
			throw e;

		} catch (Exception e) {
			logger.log(e);
		}
		return null;
	}

	@Override
	public TaskResult getResult(Map<String, String> requestArguments,
			TaskContext taskContext) throws TimeoutException {

		Map<String, Serializable> resultMap;

		UUID taskId = UUID.randomUUID();
		try {
			TaskStartResult startResult = startTask(requestArguments,
					taskContext, taskId);
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
			responseStorage.clear(taskId);
		}

	}

	@Override
	public TaskResult cancelTask(UUID taskId) {
		try {
			List<SlaveThread> clientThreads = slaves.getListByTaskId(taskId);
			logger.log("Trying to cancel task " + taskId);
			ResponsesArray responsesArray = responseStorage.get(taskId);
			if (responsesArray != null) {
				distributionService.broadcastStopRequests(clientThreads,
						new NodeRequest(taskId, true,
								responsesArray.getMeta().getTaskName()));
				responseStorage.setStopAllResponses(taskId);
				return new TaskResult("Task " + taskId + " was canceled");
			} else {
				logger.log(Level.SEVERE, "No task was found for given task id");
				throw new IllegalArgumentException(
						"No task was found for given task id");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(WRONG_ARGUMENTS, e);
		} catch (EmptyClusterException e) {
			logger.log(Level.SEVERE, e);
			throw new IllegalStateException(
					"Can not cancel task due to empty cluster", e);
		}
	}

}
