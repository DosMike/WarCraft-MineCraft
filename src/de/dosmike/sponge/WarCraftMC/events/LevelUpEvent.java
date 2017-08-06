package de.dosmike.sponge.WarCraftMC.events;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import de.dosmike.sponge.WarCraftMC.Profile;

/** In most cases the cause should be identical to the one in a pre-run gainXPevent as the events flow is:<br>
 * <pre>DamageEntityEvent (willCauseDeath) > GainXPEvent > LevelUpEvent</pre><br>
 * The amount of XP the player got may be 0 and this event might still fire as switching to a race will
 * push 0 XP in a profile to try and give the initial perk points / levels 
 */
public class LevelUpEvent extends AbstractEvent implements TargetWarCraftEvent, Cancellable {
		
	private final Cause cause;
	private final Profile target;
    private final Optional<Player> player;
    private final int levels;
	
    public LevelUpEvent(Profile target, int levels, Cause cause) {
		this.cause = cause;
		this.target = target;
		this.levels = levels;
		this.player = Sponge.getServer().getPlayer(target.getPlayerID());
	}
    
    /** Should have as HIT_TARGET the entity that died during this fight<br>
     * and as SOURCE the Living (Player) that killed the target<br>
     * Or nothing if the level up occurred on race select / profile load
      */
	@Override
	public Cause getCause() {
		return cause;
	}

	@Override
	public Profile getWarCraftProfile() {
		return target;
	}
	@Override
	public Optional<Player> getPlayer() {
		return player;
	}
	
	/** @return the amount of levels the player is about to receive */
	public int getLevels() {
		return levels;
	}
	/** @return the level the player would have after this event passes or 0 if no racedata are available*/
	public int getFinalLevel() {
		if (!target.getRaceData().isPresent()) return 0;
		return levels+target.getRaceData().get().getLevel();
	}
	
	private boolean cancelled=false;
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	@Override
	public void setCancelled(boolean cancel) {
		cancelled=cancel;
	}
	
}
