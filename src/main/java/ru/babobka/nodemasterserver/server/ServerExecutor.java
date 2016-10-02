package ru.babobka.nodemasterserver.server;

import ru.babobka.nodemasterserver.model.ServerContext;

public class ServerExecutor {

	private final MasterServer server;

	private volatile boolean starting;

	private volatile boolean stopping;

	private volatile Thread runThread;

	public ServerExecutor(MasterServer server) {
		this.server = server;
	}

	public boolean isRunning() {
		while (starting || stopping) {
			Thread.yield();
		}
		return server.isRunning();
	}

	public boolean isStopped() {
		while (starting || stopping) {
			Thread.yield();
		}
		return server.isStopped();
	}

	public void stop() {
		while (starting) {
			Thread.yield();
		}
		if (server.isRunning()) {
			synchronized (this) {
				if (server.isRunning()) {
					try {
						stopping = true;
						ServerContext.getInstance().getLogger().log("Try to stop MasterServer");
						server.stop();
					} finally {
						stopping = false;
					}
				}
			}
		}
	}

	public void run() {
		if (!server.isRunning() && !starting) {
			synchronized (this) {
				if (!server.isRunning() && !starting) {
					ServerContext.getInstance().getLogger().log("MasterServer run");
					starting = true;
					if (runThread != null && runThread.isAlive()) {
						try {
							runThread.join();
						} catch (InterruptedException e) {
							runThread.interrupt();
							ServerContext.getInstance().getLogger().log(e);
						}
					}
					runThread = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								server.run();
								starting = false;
							} catch (Exception e) {
								ServerContext.getInstance().getLogger().log(e);
							}

						}
					});
					runThread.start();
				}
			}
		}
	}

}
