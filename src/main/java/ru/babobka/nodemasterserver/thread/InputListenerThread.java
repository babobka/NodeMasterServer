package ru.babobka.nodemasterserver.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.Slaves;

/**
 * Created by dolgopolov.a on 27.07.15.
 */
public class InputListenerThread extends Thread {

	private final ServerSocket ss;

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	private final Slaves slaves = Container.getInstance().get(Slaves.class);

	public InputListenerThread(int port) throws IOException {
		ss = new ServerSocket(port);
	}

	@Override
	public void run() {
		try {
			logger.log("Start InputListenerThread");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket socket = ss.accept();
					if (slaves.isFittable()) {
						new SlaveThread(socket).start();
					} else {
						logger.log(Level.WARNING, "Can not fit new slave");
						socket.close();
					}
				} catch (Exception e) {
					if (!ss.isClosed()
							|| !Thread.currentThread().isInterrupted()) {
						logger.log(e);
					}

				}

			}
		} finally {

			try {
				ss.close();
			} catch (IOException e) {
				logger.log(e);
			}

		}
		logger.log("InputListenerThread is done");
	}

	@Override
	public void interrupt() {
		super.interrupt();
		try {
			ss.close();
		} catch (IOException e) {
			logger.log(e);
		}

	}
}