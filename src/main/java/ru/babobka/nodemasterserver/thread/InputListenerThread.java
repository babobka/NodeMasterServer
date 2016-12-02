package ru.babobka.nodemasterserver.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import ru.babobka.nodemasterserver.server.MasterServerContext;

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
			MasterServerContext.getInstance().getLogger().log("Start InputListenerThread");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket socket = ss.accept();
					if (MasterServerContext.getInstance().getSlaves().isFittable()) {
						new SlaveThread(socket).start();
					} else {
						MasterServerContext.getInstance().getLogger().log(Level.WARNING,"Can not fit new slave");
						socket.close();
					}
				} catch (Exception e) {
					if (!ss.isClosed() || !Thread.currentThread().isInterrupted()) {
						MasterServerContext.getInstance().getLogger().log(e);
					}

				}

			}
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					MasterServerContext.getInstance().getLogger().log(e);
				}
			}
		}
		MasterServerContext.getInstance().getLogger().log("InputListenerThread is done");
	}

	@Override
	public void interrupt() {
		super.interrupt();
		try {
			ss.close();
		} catch (IOException e) {
			MasterServerContext.getInstance().getLogger().log(e);
		}

	}
}