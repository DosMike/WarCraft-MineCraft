package de.dosmike.sponge.WarCraftMC.events;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.races.Race;

/**
 * The cause may hole the command source responsible for changing the race. This does not have to be the target player!
 */
public class ChangeRaceEvent extends AbstractChangeEvent<Race> implements TargetWarCraftEvent, Cancellable {
	
	private final Cause cause;
	private final Profile target;
    private final Optional<Player> player;
    private final Optional<Race> previous;
    private final Optional<Race> current;
	
    public ChangeRaceEvent(Profile target, Race previous, Race current) {
		this.cause = Sponge.getCauseStackManager().getCurrentCause();
		this.target = target;
		this.player = Sponge.getServer().getPlayer(target.getPlayerID());
		this.previous = previous==null?Optional.empty():Optional.of(previous);
		this.current = current==null?Optional.empty():Optional.of(current);
	}
    
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
	
	@Override
	public Optional<Race> getChangeFrom() {
		return previous;
	}
	
	@Override
	public Optional<Race> getChangeTo() {
		return current;
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
