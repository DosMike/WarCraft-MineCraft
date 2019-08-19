package de.dosmike.sponge.WarCraftMC.events;

import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.Optional;

public abstract class AbstractChangeEvent<T> extends AbstractEvent {
	
	/** @return the value that this event is changing from or empty if there was no previous value */
	public abstract Optional<T> getChangeFrom();
	/** @return the value that this event is changing to or empty if this value is about to be emptied */
	public abstract Optional<T> getChangeTo();
}
