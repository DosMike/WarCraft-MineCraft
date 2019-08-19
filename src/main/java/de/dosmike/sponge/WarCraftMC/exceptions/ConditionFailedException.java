package de.dosmike.sponge.WarCraftMC.exceptions;

@SuppressWarnings("serial")
public class ConditionFailedException extends Exception {
	public ConditionFailedException() {
		super();
	}
	public ConditionFailedException(String message) {
		super(message);
	}
	public ConditionFailedException(Throwable throwable) {
		super(throwable);
	}
	public ConditionFailedException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
