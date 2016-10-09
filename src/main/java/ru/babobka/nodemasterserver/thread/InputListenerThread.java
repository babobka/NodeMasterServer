package ru.babobka.nodemasterserver.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ru.babobka.nodemasterserver.server.ServerContext;

/**
 * Created by dolgopolov.a on 27.07.15.
 */
public class InputListenerThread extends Thread {

	private final ServerSocket ss;

	public InputListenerThread(int port) throws IOException {
		ss = new ServerSocket(port);
	}

	@Override
	public void run() {
		try {
			ServerContext.getInstance().getLogger().log("Start InputListenerThread");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket socket = ss.accept();
					new ClientThread(socket).start();
				} catch (Exception e) {
					if (!ss.isClosed() || !Thread.currentThread().isInterrupted()) {
						ServerContext.getInstance().getLogger().log(e);
					}

				}

			}
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					ServerContext.getInstance().getLogger().log(e);
				}
			}
		}
		ServerContext.getInstance().getLogger().log("InputListenerThread is done");
	}

	@Override
	public void interrupt() {
		super.interrupt();
		try {
			ss.close();
		} catch (IOException e) {
			ServerContext.getInstance().getLogger().log(e);
		}

	}
}