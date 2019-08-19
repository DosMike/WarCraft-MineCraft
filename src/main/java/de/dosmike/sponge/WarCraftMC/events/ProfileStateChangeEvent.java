package de.dosmike.sponge.WarCraftMC.events;

import de.dosmike.sponge.WarCraftMC.Profile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.Optional;

/** this skill is also used internally to disable and re-enable all WC features and skills on a player.
 * E.g.: In case the player switches the world, the StateEffectMonitor will be notified and all changes
 * to the player will be reverted if possible (it's still pvp).
 */
public class ProfileStateChangeEvent extends AbstractEvent implements TargetWarCraftEvent {
		
	private final Cause cause;
	private final Profile target;
    private final Optional<Player> player;
    private final boolean active;
	
    public ProfileStateChangeEvent(Profile target, boolean active) {
		this.cause = Sponge.getCauseStackManager().getCurrentCause();
		this.target = target;
		this.active = active;
		this.player = Sponge.getServer().getPlayer(target.getPlayerID());
	}
    
    /** There's nothing really to get here first player (SOURCE) is player
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
	
	/** @return if this is true the player is now actively in WarCode */
	public boolean isProfileActive() {
		return active;
	}
	
}
