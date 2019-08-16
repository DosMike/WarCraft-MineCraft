package de.dosmike.sponge.WarCraftMC.exceptions;

@SuppressWarnings("serial")
public class DuplicateIDException extends Exception {
	public DuplicateIDException() {
		super();
	}
	public DuplicateIDException(String message) {
		super(message);
	}
	public DuplicateIDException(Throwable throwable) {
		super(throwable);
	}
	public DuplicateIDException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
