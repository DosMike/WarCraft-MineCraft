package de.dosmike.sponge.WarCraftMC.exceptions;

@SuppressWarnings("serial")
public class ResolveConditionException extends RuntimeException {
	public ResolveConditionException() {
		super();
	}
	public ResolveConditionException(String message) {
		super(message);
	}
	public ResolveConditionException(Throwable throwable) {
		super(throwable);
	}
	public ResolveConditionException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
