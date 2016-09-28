package ru.babobka.nodeServer.exception;

public class InvalidUserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3603894271678084823L;

	public InvalidUserException() {
		super();
	}

	public InvalidUserException(String message) {
		super(message);
	}

	public InvalidUserException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidUserException(Throwable cause) {
		super(cause);
	}
}
