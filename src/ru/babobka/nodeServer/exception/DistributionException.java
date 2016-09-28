package ru.babobka.nodeServer.exception;

public class DistributionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 471658253997347795L;

	public DistributionException() {
		super();
	}

	public DistributionException(String message) {
		super(message);
	}

	public DistributionException(String message, Throwable cause) {
		super(message, cause);
	}

	public DistributionException(Throwable cause) {
		super(cause);
	}
}
