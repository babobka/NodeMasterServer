package ru.babobka.nodemasterserver.exception;

public class AuthFailException extends Exception {



	/**
	 * 
	 */
	private static final long serialVersionUID = 471658253997347795L;

	public AuthFailException() {
		super();
	}

	public AuthFailException(String message) {
		super(message);
	}

	public AuthFailException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthFailException(Throwable cause) {
		super(cause);
	}
}
