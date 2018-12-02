package de.dosmike.sponge.WarCraftMC.events;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import de.dosmike.sponge.WarCraftMC.races.Skill;

public class UseSkillEvent extends AbstractEvent implements TargetWarCraftEvent, Cancellable {
		
	private final Cause cause;
	private final Profile target;
    private final Optional<Player> player;
	private final Skill skill;
	private final ActionData data;
	
    public UseSkillEvent(Profile target, Skill skill, ActionData data) {
		this.cause = Sponge.getCauseStackManager().getCurrentCause();
		this.target = target;
		this.skill = skill;
		this.data = data;
		this.player = Sponge.getServer().getPlayer(target.getPlayerID());
	}
    
    /** Should have as HIT_TARGET the opponent<br>
     * and as SOURCE the Living (Player) "self"
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
	
	/** @return the skill used for this event */
	public Skill getSkill() {
		return skill;
	}
	/** @return all additional data that will be used to fire the skill */
	public ActionData getActionData() {
		return data;
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
