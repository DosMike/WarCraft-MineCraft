package de.dosmike.sponge.WarCraftMC.events;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import de.dosmike.sponge.WarCraftMC.Profile;


/** This event will fire as the player gains xp. While handling the event the xp will hold the original ammount. <br>
 * After the event was handled if it was not canceled the xp will be added to the profile and a level up event may follow.<br>
 * The cause may contain source and hit_target named causes if this event was called by the damage manager after a living was killed.
 * source will then be the player slaying the living (if it did not die otherwise) and hit_target will be the (now dead) mob. 
 */
public class GainXPEvent extends AbstractEvent implements TargetWarCraftEvent, Cancellable {
		
	private final Cause cause;
	private final Profile target;
    private final Optional<Player> player;
    private long xp;
	
    public GainXPEvent(Profile target, long XP) {
		this.cause = Sponge.getCauseStackManager().getCurrentCause();
		this.target = target;
		this.player = Sponge.getServer().getPlayer(target.getPlayerID());
		this.xp = XP;
	}
    
    /** Should have as HIT_TARGET the entity that died during this fight<br>
     * and as SOURCE the Living (Player) that killed the target
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
	
	public long getXP() {
		return xp;
	}
	public void setXP(long newXP) {
		xp = (newXP>xp?0:xp-newXP);
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
