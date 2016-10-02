package ru.babobka.nodemasterserver.exception;

public class CanNotInitTaskFactoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 471658253997347795L;

	public CanNotInitTaskFactoryException() {
		super();
	}

	public CanNotInitTaskFactoryException(String message) {
		super(message);
	}

	public CanNotInitTaskFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public CanNotInitTaskFactoryException(Throwable cause) {
		super(cause);
	}
}
