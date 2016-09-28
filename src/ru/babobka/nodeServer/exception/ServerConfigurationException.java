package ru.babobka.nodeServer.exception;

public class ServerConfigurationException  extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3603894271678084823L;

	public ServerConfigurationException() {
		super();
	}

	public ServerConfigurationException(String message) {
		super(message);
	}

	public ServerConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerConfigurationException(Throwable cause) {
		super(cause);
	}
}
