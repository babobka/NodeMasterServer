package ru.babobka.nodemasterserver.runnable;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import ru.babobka.nodemasterserver.server.ServerContext;
import ru.babobka.nodemasterserver.thread.ClientThread;

public class HeartBeatingRunnable implements Runnable {

	@Override
	public void run() {

		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(ServerContext.getInstance().getConfig().getHeartBeatTimeOutMillis());
				List<ClientThread> clientThreads = ServerContext.getInstance().getClientThreads().getFullList();
				for (ClientThread clientThread : clientThreads) {
					if (!Thread.currentThread().isInterrupted()) {
						try {
							clientThread.sendHeartBeating();
						} catch (IOException e) {
							ServerContext.getInstance().getLogger().log(e);
						}
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				ServerContext.getInstance().getLogger().log(Level.WARNING, e);
				break;
			}

		}
	}

}
