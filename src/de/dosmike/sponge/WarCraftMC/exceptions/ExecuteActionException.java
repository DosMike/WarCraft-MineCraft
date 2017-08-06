package de.dosmike.sponge.WarCraftMC.exceptions;

@SuppressWarnings("serial")
public class ExecuteActionException extends Exception {
	public ExecuteActionException() {
		super();
	}
	public ExecuteActionException(String message) {
		super(message);
	}
	public ExecuteActionException(Throwable throwable) {
		super(throwable);
	}
	public ExecuteActionException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
