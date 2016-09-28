package ru.babobka.nodeServer.model;

import ru.babobka.nodeServer.exception.FullClusterException;
import ru.babobka.nodeServer.thread.ClientThread;
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
public class ClientThreads {

	private final AtomicReferenceArray<ClientThread> threads;

	private final AtomicInteger size;

	public ClientThreads(int maxSize) {
		this.threads = new AtomicReferenceArray<>(maxSize);
		size = new AtomicInteger(0);
	}

	public List<ClusterUser> getCurrentClusterUserList() {
		List<ClusterUser> clusterUserList = new ArrayList<>();
		ClientThread ct;
		for (int i = 0; i < threads.length(); i++) {
			if ((ct = threads.get(i)) != null) {
				clusterUserList
						.add(new ClusterUser(ct.getLogin(), ct.getSocket().getLocalPort(), ct.getSocket().getPort(),
								ct.getSocket().getInetAddress().getCanonicalHostName(), ct.getRequestCount()));
			}
		}

		return clusterUserList;
	}

	public synchronized void remove(ClientThread ct) {
		for (int i = 0; i < threads.length(); i++) {
			if (threads.get(i) == ct) {
				threads.set(i, null);
				size.decrementAndGet();
				return;
			}
		}
	}

	public synchronized void add(ClientThread ct) throws FullClusterException {

		for (int i = 0; i < threads.length(); i++) {
			if (threads.get(i) == null) {
				threads.set(i, ct);
				size.incrementAndGet();
				return;
			}
		}
		throw new FullClusterException();
	}

	public synchronized List<ClientThread> getFullList() {
		ArrayList<ClientThread> clientThreadList = new ArrayList<>();
		for (int i = 0; i < threads.length(); i++) {
			if (threads.get(i) != null) {
				clientThreadList.add(threads.get(i));
			}
		}
		Collections.shuffle(clientThreadList);
		return clientThreadList;
	}

	public List<ClientThread> getList(String taskName) {
		ArrayList<ClientThread> clientThreadList = new ArrayList<>();
		ClientThread ct;
		for (int i = 0; i < threads.length(); i++) {
			ct = threads.get(i);
			if (ct != null && ct.getTaskSet() != null && ct.getTaskSet().contains(taskName)) {
				clientThreadList.add(ct);
			}
		}
		Collections.shuffle(clientThreadList);
		return clientThreadList;
	}

	public List<ClientThread> getListByTaskId(long taskId) {
		ArrayList<ClientThread> clientThreadList = new ArrayList<>();
		ClientThread ct;
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

	public int getClusterSize(String taskName) {
		int j = 0;
		ClientThread ct;
		for (int i = 0; i < threads.length(); i++) {
			if ((ct = threads.get(i)) != null && ct.getTaskSet() != null && ct.getTaskSet().contains(taskName)) {
				j++;
			}
		}
		return j;
	}

	public boolean isEmpty() {
		return size.intValue() == 0;
	}

}
