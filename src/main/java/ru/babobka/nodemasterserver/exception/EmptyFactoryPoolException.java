package ru.babobka.nodemasterserver.exception;

/**
 * Created by dolgopolov.a on 22.12.15.
 */
public class EmptyFactoryPoolException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3573280529720344853L;

	//Parameterless Constructor
    public EmptyFactoryPoolException() {}

    //Constructor that accepts a message
    public EmptyFactoryPoolException(String message)
    {
        super(message);
    }

}
