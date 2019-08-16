package de.dosmike.sponge.WarCraftMC.Manager;

import de.dosmike.sponge.WarCraftMC.ManaPipe;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.SpongeEventListeners;
import de.dosmike.sponge.WarCraftMC.XPpipe;
import de.dosmike.sponge.WarCraftMC.effects.WarCraftCustomEffect;
import de.dosmike.sponge.WarCraftMC.events.ProfileStateChangeEvent;
import de.dosmike.sponge.mikestoolbox.living.BoxLiving;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerStateManager {
	static Set<UUID> active = new HashSet<>(); //track if player leave the warcraft area
	static Set<UUID> disconnecting = new HashSet<>(); //if the diconnect event forced player out, we do not want to re-add them
	
	public static void tick() {
		Set<UUID> online=new HashSet<>();
		for (Player p : Sponge.getServer().getOnlinePlayers()) {
			if (!p.isLoaded() || p.get(Keys.HEALTH).orElse(0.0)<=0.0) { continue; }
			Optional<Profile> pro = Profile.getIfOnline(p.getUniqueId());
			if (!pro.isPresent()) continue;
			
			UUID id = p.getUniqueId();
			if (disconnecting.contains(id)) continue;
			online.add(id);
			boolean now = Profile.isActive(p, pro.get());
			boolean was = active.contains(id);
			
			if (now && !was) {
				active.add(id);
//				Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), now, new EventCause(p).get()));
				Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), now));
			} else if (was && !now) {
				active.remove(id);
//				Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), now, new EventCause(p).get()));
				Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), now));
			}
		}
		active.retainAll(online); //remove players offline
		disconnecting.retainAll(online);
	}
	public static void forceOut(Player player) {
		SpongeEventListeners.restoreKeyedDefaults(player);
		disconnecting.add(player.getUniqueId());
		active.remove(player.getUniqueId());
		Optional<Profile> pro = Profile.getIfOnline(player.getUniqueId());
		if (!pro.isPresent()) return;
//		Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), false, new EventCause(player).get()));
		//updates state to inactive, that in turn removes effects
		Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), false));
	}

	/** utility to remove all race modifications to a player on e.g. race change
	 * or the player stops participating in warcraft by e.g. a world change
	 * @param optionalProfile can be passed in, if the caller happens to have it,
	 *                        otherwise will be retrieved
	 */
	public static void resetPlayerFx(Player player, Profile optionalProfile) {
//		NextSpawnActionManager.removeAll(player);
		if (optionalProfile == null) optionalProfile = Profile.loadOrCreate(player);
		BoxLiving.removeCustomEffect(player, WarCraftCustomEffect.class); //remove all effects
		XPpipe.restoreVanilla(player, optionalProfile);
		SpongeEventListeners.restoreKeyedDefaults(player);
		ManaPipe.dropPlayer(player);
	}
}
