package de.dosmike.sponge.WarCraftMC.Manager;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.XPpipe;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("serial")
public class EntityDamageHolder extends HashMap<UUID, Double> {
	public void increase(UUID player, Double damage) {
		put(player, (containsKey(player)?get(player):0.0)+damage);
	}
	/** pays damage inflicted * multiplier xp to the combatants.<br>
	 * To avoid 0 xp values the xp are Math.round()ed instead of just long casted 
	 * @param cause will be used later for GainXPEvent should be ready to read */
	public void payXP(double multiplier) {
		for (Entry<UUID, Double> damage : entrySet()) {
			Optional<Profile> profile = Profile.getIfOnline(damage.getKey());
			if (!profile.isPresent()) continue;
			XPpipe.processWarCraftXP(profile.get(), Math.round(damage.getValue()*multiplier));
		}
		clear(); //prevent multiple pay-outs
	}
}
