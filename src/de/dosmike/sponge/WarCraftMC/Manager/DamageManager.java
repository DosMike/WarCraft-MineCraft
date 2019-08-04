package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;

/** Responsible for tracking damage and awarding xp as a entity dies.<br>
 * This is done to give players a fair share, instead of a LoL-like last hit takes it all */
public class DamageManager {
	
	/** mapping victim > attacker > damage */
	static Map<UUID, EntityDamageHolder> damageMonitor = new HashMap<>();
	/** mapping attackers > victims */
	static Map<UUID, Set<UUID>> entityMonitor = new HashMap<>();
	
	/** register damage
	 * @param source unless anyone plans to add npcs I'll leave this Player
	 * @param target the entity hit by the player, if not a living we really don't care, where's the challenge?
	 * @param damage the damage source inflicted on target */
	public static void damage(Player source, Living target, Double damage) {
		damage(source.getUniqueId(), target.getUniqueId(), damage);
	}
	
	/** the safer register damage, Player/Living instances are not guaranteed to last!
	 * @param source unless anyone plans to add npcs I'll leave this Player
	 * @param target the entity hit by the player, if not a living we really don't care, where's the challenge?
	 * @param damage the damage source inflicted on target */
	public static void damage(UUID source, UUID target, Double damage) {
		Set<UUID> v = (entityMonitor.containsKey(source)
				? entityMonitor.get(source)
				: new HashSet<UUID>());
		v.add(target);
		entityMonitor.put(source, v);
		
		EntityDamageHolder h = (damageMonitor.containsKey(target)
				? damageMonitor.get(target)
				: new EntityDamageHolder());
		h.increase(source, damage);
		damageMonitor.put(target, h);
	}
	
	public static void remove(Player source) {
		remove(source.getUniqueId());
	}
	public static void remove(UUID source) {
		if (!entityMonitor.containsKey(source)) return;
		entityMonitor.get(source).forEach(target -> {
			EntityDamageHolder h = damageMonitor.get(target);
			if (h == null) return;
			h.remove(source);
			if (h.isEmpty()) damageMonitor.remove(target);
			else damageMonitor.put(target, h);
		});
		entityMonitor.remove(source);
	}
	
	/** resolve xp by dealth damage * multiplier for the not dead entity
	 * @param entity is the entity that dies
	 * @param multiplier is a additional xp multiplier to apply */
	public static void death(UUID entity, double multiplier) {
		if (!damageMonitor.containsKey(entity)) return;
		EntityDamageHolder h = damageMonitor.get(entity);
		Set<UUID> fighters = new HashSet<>();
		fighters.addAll(h.keySet());
		h.payXP(multiplier);
		damageMonitor.remove(entity);
		fighters.forEach(fighter -> {
			Set<UUID> victims = entityMonitor.get(fighter);
			if (victims == null) return;
			victims.remove(entity);
			if (victims.isEmpty()) entityMonitor.remove(fighter);
			else entityMonitor.put(fighter, victims);
		});
	}
}
