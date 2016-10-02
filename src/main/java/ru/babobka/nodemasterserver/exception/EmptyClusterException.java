package ru.babobka.nodemasterserver.exception;

/**
 * Created by dolgopolov.a on 22.12.15.
 */
public class EmptyClusterException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3224954994656123763L;

	public EmptyClusterException() {
        super();
    }

    public EmptyClusterException(String message) {
        super(message);
    }

    public EmptyClusterException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyClusterException(Throwable cause) {
        super(cause);
    }
}
