package ru.babobka.nodemasterserver.model;

import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodeserials.NodeRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Created by dolgopolov.a on 28.07.15.
 */
public class Slaves {

	private final AtomicReferenceArray<SlaveThread> threads;

	private final AtomicInteger size = new AtomicInteger(0);

	public Slaves(int maxSize) {
		this.threads = new AtomicReferenceArray<>(maxSize);
	}

	public synchronized boolean isFittable() {
		return size.get() <= threads.length();
	}

	public synchronized List<ClusterUser> getCurrentClusterUserList() {
		List<ClusterUser> clusterUserList = new ArrayList<>();
		SlaveThread st;
		for (int i = 0; i < threads.length(); i++) {
			if ((st = threads.get(i)) != null) {
				clusterUserList
						.add(new ClusterUser(st.getLogin(), st.getSocket().getLocalPort(), st.getSocket().getPort(),
								st.getSocket().getInetAddress().getCanonicalHostName(), st.getRequestCount()));
			}
		}

		return clusterUserList;
	}

	public synchronized boolean remove(SlaveThread slave) {
		if (slave != null) {
			for (int i = 0; i < threads.length(); i++) {
				if (threads.get(i) == slave) {
					threads.set(i, null);
					size.decrementAndGet();
					return true;

				}

			}
		}
		return false;
	}

	public synchronized boolean add(SlaveThread slave) {
		if (slave != null && size.intValue() != threads.length()) {
			for (int i = 0; i < threads.length(); i++) {
				if (threads.get(i) == null) {
					threads.set(i, slave);
					size.incrementAndGet();
					return true;

				}

			}
		}
		return false;
	}

	public synchronized List<SlaveThread> getFullList() {
		ArrayList<SlaveThread> slaveThreadList = new ArrayList<>();
		for (int i = 0; i < threads.length(); i++) {
			SlaveThread st = threads.get(i);
			if (st != null) {
				slaveThreadList.add(st);
			}
		}
		Collections.shuffle(slaveThreadList);
		return slaveThreadList;
	}

	public synchronized List<SlaveThread> getList(String taskName) {
		return getList(taskName, -1);
	}

	public synchronized List<SlaveThread> getList(String taskName, int maxThreads) {
		List<SlaveThread> slaveThreadList = new ArrayList<>();
		SlaveThread st;
		for (int i = 0; i < threads.length(); i++) {
			st = threads.get(i);
			if (st != null && st.getAvailableTasksSet() != null && st.getAvailableTasksSet().contains(taskName)) {
				slaveThreadList.add(st);
			}
		}
		Collections.shuffle(slaveThreadList);
		if (maxThreads != -1 && maxThreads < slaveThreadList.size()) {
			return slaveThreadList.subList(0, maxThreads);
		}
		return slaveThreadList;
	}

	public synchronized List<SlaveThread> getListByTaskId(UUID taskId) {
		List<SlaveThread> slaveThreadList = new ArrayList<>();
		SlaveThread st;
		for (int i = 0; i < threads.length(); i++) {
			st = threads.get(i);
			if (st != null && !st.getRequestMap().isEmpty()) {
				for (Map.Entry<UUID, NodeRequest> requestEntry : st.getRequestMap().entrySet()) {
					if (requestEntry.getValue().getTaskId().equals(taskId)) {
						slaveThreadList.add(st);
						break;
					}
				}
			}

		}
		Collections.shuffle(slaveThreadList);
		return slaveThreadList;
	}

	public int getClusterSize() {
		return size.intValue();
	}

	public synchronized int getClusterSize(String taskName) {
		int counter = 0;
		SlaveThread st;
		for (int i = 0; i < threads.length(); i++) {
			st = threads.get(i);
			if (st != null && st.getAvailableTasksSet().contains(taskName)) {
				counter++;
			}
		}
		return counter;
	}

	private synchronized void interruptAll() {

		List<SlaveThread> clientThreadsList = getFullList();
		for (SlaveThread st : clientThreadsList) {
			st.interrupt();
		}
	}

	public synchronized void clear() {

		if (!isEmpty()) {
			interruptAll();
			for (int i = 0; i < threads.length(); i++) {
				threads.set(i, null);
			}
			size.set(0);
		}

	}

	public boolean isEmpty() {
		return size.intValue() == 0;
	}

}
