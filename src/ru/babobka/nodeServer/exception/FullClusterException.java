package ru.babobka.nodeServer.exception;

public class FullClusterException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3573280529720344853L;

	//Parameterless Constructor
    public FullClusterException() {}

    //Constructor that accepts a message
    public FullClusterException(String message)
    {
        super(message);
    }

}
