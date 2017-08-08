package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.events.EventCause;
import de.dosmike.sponge.WarCraftMC.events.ProfileStateChangeEvent;

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
			boolean now = pro.get().isActive(p);
			boolean was = active.contains(id);
			
			if (now && !was) {
				active.add(id);
				Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), now, new EventCause(p).get()));
			} else if (was && !now) {
				active.remove(id);
				Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), now, new EventCause(p).get()));
			}
		}
		active.retainAll(online); //remove players offline
		disconnecting.retainAll(online);
	}
	public static void forceOut(Player player) {
		disconnecting.add(player.getUniqueId());
		active.remove(player.getUniqueId());
		Optional<Profile> pro = Profile.getIfOnline(player.getUniqueId());
		if (!pro.isPresent()) return;
		Sponge.getEventManager().post(new ProfileStateChangeEvent(pro.get(), false, new EventCause(player).get()));
	}
}
