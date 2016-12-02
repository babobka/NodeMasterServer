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
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.task.TaskContext;
import ru.babobka.nodemasterserver.task.TaskPool;
import ru.babobka.nodemasterserver.task.TaskResult;
import ru.babobka.nodemasterserver.task.TaskStartResult;
import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodemasterserver.util.DistributionUtil;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.subtask.model.RequestDistributor;
import ru.babobka.subtask.model.SubTask;

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

	private void startTask(TaskContext taskContext, long taskId, Map<String, String> arguments)
			throws EmptyClusterException, DistributionException {
		int currentClusterSize;
		String taskName = taskContext.getConfig().getName();
		if (isRequestDataIsTooSmall(taskContext.getTask(), arguments)) {
			currentClusterSize = 1;
		} else {
			currentClusterSize = MasterServerContext.getInstance().getSlaves().getClusterSize(taskName);
		}
		MasterServerContext.getInstance().getResponseStorage().put(taskId,
				new ResponsesArray(currentClusterSize, taskContext, arguments));
		NodeRequest[] requests = taskContext.getTask().getDistributor().distribute(arguments, currentClusterSize,
				taskId);
		DistributionUtil.broadcastRequests(taskName, requests);

	}

	private boolean isRequestDataIsTooSmall(SubTask task, Map<String, String> arguments) {
		NodeRequest request = task.getDistributor().distribute(arguments, 1, 0)[0];
		if (task.isRequestDataTooSmall(request.getAddition())) {
			return true;
		}
		return false;

	}

	private TaskStartResult startTask(Map<String,String> requestArguments, TaskContext taskContext, long taskId) {

		RequestDistributor requestDistributor = taskContext.getTask().getDistributor();
		if (requestDistributor.isValidArguments(requestArguments)) {
			MasterServerContext.getInstance().getLogger().log("Task id is " + taskId);
			try {
				startTask(taskContext, taskId, requestArguments);
				return new TaskStartResult(taskId);
			} catch (DistributionException e) {
				MasterServerContext.getInstance().getLogger().log(Level.SEVERE, e);
				try {
					DistributionUtil.broadcastStopRequests(
							MasterServerContext.getInstance().getSlaves().getListByTaskId(taskId),
							new NodeRequest(taskId, true, taskContext.getConfig().getName()));
				} catch (EmptyClusterException e1) {
					MasterServerContext.getInstance().getLogger().log(e1);
				}
				return new TaskStartResult(taskId, true, true, "Can not distribute your request");
			} catch (EmptyClusterException e) {
				MasterServerContext.getInstance().getLogger().log(e);
				return new TaskStartResult(taskId, true, true, "Can not distribute due to empty cluster");
			}

		} else {
			MasterServerContext.getInstance().getLogger().log(Level.SEVERE, WRONG_ARGUMENTS);
			return new TaskStartResult(taskId, true, false, WRONG_ARGUMENTS);
		}

	}

	private Map<String, Serializable> getTaskResult(long taskId) {
		try {
			ResponseStorage responseStorage = MasterServerContext.getInstance().getResponseStorage();
			ResponsesArray responsesArray = responseStorage.get(taskId);
			if (responsesArray != null) {
				return taskPool.get(responsesArray.getMeta().getTaskName()).getTask().getReducer()
						.reduce(responsesArray.getResponseList());

			} else {
				MasterServerContext.getInstance().getLogger().log(Level.SEVERE, "No such task");
			}
		} catch (Exception e) {
			MasterServerContext.getInstance().getLogger().log(e);
		}
		return null;
	}

	@Override
	public TaskResult getResult(Map<String,String> requestArguments, TaskContext taskContext) {

		Map<String, Serializable> resultMap;

		Long taskId = (long) (Math.random() * Integer.MAX_VALUE);
		try {
			TaskStartResult startResult = startTask(requestArguments, taskContext, taskId);
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
			MasterServerContext.getInstance().getResponseStorage().clear(taskId);
		}

	}

	@Override
	public TaskResult cancelTask(long taskId) {
		try {
			List<SlaveThread> clientThreads = MasterServerContext.getInstance().getSlaves().getListByTaskId(taskId);
			MasterServerContext.getInstance().getLogger().log("Trying to cancel task " + taskId);
			ResponsesArray responsesArray = MasterServerContext.getInstance().getResponseStorage().get(taskId);
			if (responsesArray != null) {
				DistributionUtil.broadcastStopRequests(clientThreads,
						new NodeRequest(taskId, true, responsesArray.getMeta().getTaskName()));
				MasterServerContext.getInstance().getResponseStorage().setStopAllResponses(taskId);
				return new TaskResult("Task " + taskId + " was canceled");
			} else {
				MasterServerContext.getInstance().getLogger().log(Level.SEVERE, "No task was found for given task id");
				throw new IllegalArgumentException("No task was found for given task id");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(WRONG_ARGUMENTS, e);
		} catch (EmptyClusterException e) {
			MasterServerContext.getInstance().getLogger().log(Level.SEVERE, e);
			throw new IllegalStateException("Can not cancel task due to empty cluster", e);
		}
	}

}
