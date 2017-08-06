package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.event.cause.Cause;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.XPpipe;

@SuppressWarnings("serial")
public class EntityDamageHolder extends HashMap<UUID, Double> {
	public void increase(UUID player, Double damage) {
		put(player, (containsKey(player)?get(player):0.0)+damage);
	}
	/** @param cause will be used later for GainXPEvent should be ready to read */
	public void payXP(double multiplier, Cause cause) {
		for (Entry<UUID, Double> damage : entrySet()) {
			Optional<Profile> profile = Profile.getIfOnline(damage.getKey());
			if (!profile.isPresent()) continue;
			XPpipe.processWarCraftXP(profile.get(), (long)(damage.getValue()*multiplier), cause);
		}
		clear(); //prevent multiple pay-outs
	}
}
