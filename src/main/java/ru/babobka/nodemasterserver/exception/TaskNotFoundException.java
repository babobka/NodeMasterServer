package ru.babobka.nodemasterserver.exception;

public class TaskNotFoundException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4250831592405138720L;

	

	public TaskNotFoundException() {
		super();
	}

	public TaskNotFoundException(String message) {
		super(message);
	}

	public TaskNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaskNotFoundException(Throwable cause) {
		super(cause);
	}
}
