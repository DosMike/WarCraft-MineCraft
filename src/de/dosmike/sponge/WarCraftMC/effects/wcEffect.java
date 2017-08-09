package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.entity.living.Living;

/** why is this a thing and why do it "duplicate" vanilla effects with it?<br>
 * As far as I know vanilla effects apply for time unit seconds. I want effects to be able to last less.
 * Also with this I can easily revert changes dont to a player as he e.g. leaves the world */
public interface wcEffect {
	/** the name of this wcEffect if necessary */
	public String getName();
	/** how long this effect will stay active. a duration <= 0 will be permanent */
	public double getDuration();
	/** if this returns true the effect will not be added to the queue saving the hassle of removing it the next tick.<br>
	 * As a result it will only call the onApply and ignore onTick and onRemove, so those can remain empty. */
	public boolean isInstant();
	
	/** do something with the entity when the effect get's added */
	public void onApply(Living entity);
	/** should be called around every 50 ms, dt will give you the exact ms since the last tick */
	public void onTick(Living entity, int dt);
	/** remove for example applied potion effects from the entity.<br>
	 * This may never be called on a effect instance if it's extended by a effect of the same class */
	public void onRemove(Living entity);
}
