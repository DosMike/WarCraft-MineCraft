package de.dosmike.sponge.WarCraftMC.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

/** focus on short methods, simple and quick. Object of change during API7 cause changes! */
public class EventCause {
	
	public static Cause WarCraftBaseCause;
	
	Cause.Builder built;
	public EventCause() {
		built = Cause.builder().from(WarCraftBaseCause);
	}
	public EventCause(Player source) {
		built = Cause.builder().from(WarCraftBaseCause);
		built.named(NamedCause.SOURCE, source);
	}
	public EventCause(NamedCause cause) {
		built = Cause.builder().from(WarCraftBaseCause);
		built.named(cause);
	}
	
	/** bake another cause onto the stack */
	public EventCause bake(String causeName, Object causeObject) {
		built.suggestNamed(causeName, causeObject);
		return this;
	}
	/** bake another cause onto the stack */
	public EventCause bake(NamedCause cause) {
		built.named(cause);
		return this;
	}
	
	public Cause get() {
		return built.build();
	}
}
