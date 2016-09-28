package ru.babobka.nodeServer.runnable;

/**
 * Created by dolgopolov.a on 31.07.15.
 */

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.thread.ClientThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

/**
 * Created by dolgopolov.a on 27.07.15.
 */
public class InputListenerRunnable implements Runnable {

	private final ServerSocket ss;

	public InputListenerRunnable(int port) throws IOException {
		ss = new ServerSocket(port);
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				Socket socket = ss.accept();
				try {
					new ClientThread(socket).start();
				} catch (Exception e) {
					Server.getLogger().log(Level.SEVERE, e);
					socket.close();
				}

			}
		} catch (IOException e) {
			Server.getLogger().log(Level.SEVERE, e);
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					Server.getLogger().log(Level.SEVERE, e);
				}
			}
		}
	}
}