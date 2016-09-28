package ru.babobka.nodeServer.runnable;

import java.io.IOException;
import java.util.List;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.thread.ClientThread;

public class HeartBeatingRunnable implements Runnable {

	@Override
	public void run() {

		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(Server.getConfigData().getHeartBeatTimeOutMillis());
				List<ClientThread> clientThreads = Server.getClientThreads().getFullList();
				for (ClientThread clientThread : clientThreads) {
					try {
						clientThread.sendHeartBeating();
					} catch (IOException e) {
						Server.getLogger().log(e);
					}
				}
			} catch (InterruptedException e) {
				Server.getLogger().log(e);
				break;
			}

		}
	}

}
