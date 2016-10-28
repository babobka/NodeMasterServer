package ru.babobka.nodemasterserver.model;

import ru.babobka.nodemasterserver.thread.SlaveThread;
import ru.babobka.nodeserials.NodeRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

	public synchronized List<ClusterUser> getCurrentClusterUserList() {
		List<ClusterUser> clusterUserList = new ArrayList<>();
		SlaveThread ct;
		for (int i = 0; i < threads.length(); i++) {
			if ((ct = threads.get(i)) != null) {
				clusterUserList
						.add(new ClusterUser(ct.getLogin(), ct.getSocket().getLocalPort(), ct.getSocket().getPort(),
								ct.getSocket().getInetAddress().getCanonicalHostName(), ct.getRequestCount()));
			}
		}

		return clusterUserList;
	}

	public boolean remove(SlaveThread slave) {
		if (slave != null) {
			for (int i = 0; i < threads.length(); i++) {
				if (threads.get(i) == slave) {
					synchronized (this) {
						if (threads.get(i) == slave) {
							threads.set(i, null);
							size.decrementAndGet();
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean add(SlaveThread slave) {
		if (slave != null && size.intValue() != threads.length()) {
			for (int i = 0; i < threads.length(); i++) {
				if (threads.get(i) == null) {
					synchronized (this) {
						if (threads.get(i) == null) {
							threads.set(i, slave);
							size.incrementAndGet();
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public synchronized List<SlaveThread> getFullList() {
		ArrayList<SlaveThread> clientThreadList = new ArrayList<>();
		for (int i = 0; i < threads.length(); i++) {
			SlaveThread ct = threads.get(i);
			if (ct != null) {
				clientThreadList.add(ct);
			}
		}
		Collections.shuffle(clientThreadList);
		return clientThreadList;
	}

	public synchronized List<SlaveThread> getList(String taskName) {
		List<SlaveThread> clientThreadList = new ArrayList<>();
		SlaveThread ct;
		for (int i = 0; i < threads.length(); i++) {
			ct = threads.get(i);
			if (ct != null && ct.getTaskSet() != null && ct.getTaskSet().contains(taskName)) {
				clientThreadList.add(ct);
			}
		}
		Collections.shuffle(clientThreadList);
		return clientThreadList;
	}

	public synchronized List<SlaveThread> getListByTaskId(long taskId) {
		List<SlaveThread> clientThreadList = new ArrayList<>();
		SlaveThread ct;
		for (int i = 0; i < threads.length(); i++) {
			ct = threads.get(i);
			if (ct != null && !ct.getRequestMap().isEmpty()) {
				for (Map.Entry<Long, NodeRequest> requestEntry : ct.getRequestMap().entrySet()) {
					if (requestEntry.getValue().getTaskId() == taskId) {
						clientThreadList.add(ct);
						break;
					}
				}
			}

		}
		Collections.shuffle(clientThreadList);
		return clientThreadList;
	}

	public int getClusterSize() {
		return size.intValue();
	}

	public synchronized int getClusterSize(String taskName) {
		int counter = 0;
		SlaveThread ct;
		for (int i = 0; i < threads.length(); i++) {
			ct = threads.get(i);
			if (ct != null && ct.getTaskSet() != null && ct.getTaskSet().contains(taskName)) {
				counter++;
			}
		}
		return counter;
	}

	private synchronized void interruptAll() {

		List<SlaveThread> clientThreadsList = getFullList();
		for (SlaveThread ct : clientThreadsList) {
			ct.interrupt();
			try {
				ct.join();
			} catch (InterruptedException e) {
				ct.interrupt();
				e.printStackTrace();
			}
		}
	}

	public synchronized void clear() {
		if (!isEmpty()) {
			synchronized (this) {
				if (!isEmpty()) {
					interruptAll();
					for (int i = 0; i < threads.length(); i++) {
						threads.set(i, null);
					}
					size.set(0);

				}
			}
		}

	}

	public boolean isEmpty() {
		return size.intValue() == 0;
	}

}
