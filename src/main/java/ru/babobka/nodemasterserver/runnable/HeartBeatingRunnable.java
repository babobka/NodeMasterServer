package ru.babobka.nodemasterserver.runnable;

import java.io.IOException;
import java.util.List;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.Slaves;
import ru.babobka.nodemasterserver.thread.SlaveThread;

public class HeartBeatingRunnable implements Runnable {

	private static final int HEARTBEAT_TIMEOUT_MILLIS = 30000;

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	private final Slaves slaves = Container.getInstance().get(Slaves.class);

	@Override
	public void run() {
		logger.log("Start HeartBeatingRunnable");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(HEARTBEAT_TIMEOUT_MILLIS);
				List<SlaveThread> clientThreads = slaves.getFullList();
				for (SlaveThread clientThread : clientThreads) {
					if (!Thread.currentThread().isInterrupted()) {
						try {
							clientThread.sendHeartBeating();
						} catch (IOException e) {
							logger.log(e);
						}
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.log("HeartBeatingRunnable is done");
				break;
			}

		}
	}

}
