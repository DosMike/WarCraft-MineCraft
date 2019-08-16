package de.dosmike.sponge.WarCraftMC.events;

import de.dosmike.sponge.WarCraftMC.Profile;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public interface TargetWarCraftEvent {
	/** Returns the Profile for the player that's about to change race.<br>
	 * You can use Profile.getPlayerID() to receive the players UUID.
	 * @return the WarCraft profile, <i><b>NOT</b></i> the GameProfile! */
	public Profile getWarCraftProfile();
	
	/** The most common action performed with the profile will be<br>
	 * receiving the target player from UUID, so the even shall provide<br>
	 * that so not every listener has to ask sponge for that information
	 * @return the Player for this profile if available */
	public Optional<Player> getPlayer();
}
