package ru.babobka.nodemasterserver.runnable;

import java.io.IOException;
import java.util.List;

import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.thread.SlaveThread;

public class HeartBeatingRunnable implements Runnable {

	private static final int HEARTBEAT_TIMEOUT_MILLIS = 30000;

	@Override
	public void run() {
		MasterServerContext.getInstance().getLogger().log("Start HeartBeatingRunnable");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(HEARTBEAT_TIMEOUT_MILLIS);
				List<SlaveThread> clientThreads = MasterServerContext.getInstance().getSlaves().getFullList();
				for (SlaveThread clientThread : clientThreads) {
					if (!Thread.currentThread().isInterrupted()) {
						try {
							clientThread.sendHeartBeating();
						} catch (IOException e) {
							MasterServerContext.getInstance().getLogger().log(e);
						}
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				MasterServerContext.getInstance().getLogger().log("HeartBeatingRunnable is done");
				break;
			}

		}
	}

}
