package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;

import de.dosmike.sponge.WarCraftMC.effects.wcEffect;

/** Manage existing and new status effects */
public class StatusEffectManager {
	/** special valus that have no other place to go */
	public static Set<UUID> frozenEntities = new HashSet<>();
	
	
	private static Map<UUID, Set<EffectHolder>> active = new HashMap<>();
	
	public static void add(Living entity, wcEffect effect) {
		effect.onApply(entity);
		if (effect.isInstant()) return;
		
		UUID at = entity.getUniqueId();
		Set<EffectHolder> fxs = active.containsKey(at)?active.get(at):new HashSet<EffectHolder>();
		Set<EffectHolder> rem = new HashSet<>();
		for (EffectHolder fx : fxs) {
			if (fx.fx.getClass().equals(effect.getClass())) {
				rem.add(fx);
			}
		}
		fxs.removeAll(rem);
		fxs.add(new EffectHolder(effect, entity));
		
		active.put(at, fxs);
	}
	public static void tick() {
		long now = System.currentTimeMillis();
		Set<UUID> rem = new HashSet<>();
		for (Entry<UUID, Set<EffectHolder>> efx : active.entrySet()) {
			Set<EffectHolder> ehs = efx.getValue();
			Set<EffectHolder> ded = new HashSet<>();
			for (EffectHolder eh : ehs) {
				if (eh.isTimedout(now)) {
					eh.fx.onRemove(eh.target);
					ded.add(eh);
				} else {
					Living meh = eh.target;
					if (!meh.isLoaded() || meh.isRemoved() || meh.get(Keys.HEALTH).orElse(0.0)<=0) { //not loaded or dieded
						eh.fx.onRemove(eh.target);
						ded.add(eh);
					} else eh.fx.onTick(eh.target);
				}
			}
			ehs.removeAll(ded);
			if (ehs.isEmpty()) rem.add(efx.getKey());
			else active.put(efx.getKey(), ehs);
		}
	}
	public static void remove(Living entity, Class<? extends wcEffect> effect) {
		Set<UUID> rem = new HashSet<>();
		for (Entry<UUID, Set<EffectHolder>> efx : active.entrySet()) {
			Set<EffectHolder> ehs = efx.getValue();
			Set<EffectHolder> ded = new HashSet<>();
			for (EffectHolder eh : ehs) {
				if (eh.fx.getClass().isAssignableFrom(effect)) {
					eh.fx.onRemove(eh.target);
					ded.add(eh);
				}
			}
			ehs.removeAll(ded);
			if (ehs.isEmpty()) rem.add(efx.getKey());
			else active.put(efx.getKey(), ehs);
		}
	}
	
	public static class EffectHolder {
		private wcEffect fx;
		private Living target;
//		private UUID targetID; // to reliably remove this from the tracker
		private long runTill; // the remaining duration 

		EffectHolder(wcEffect a, Living b) {
			fx=a;
			target=b;
//			targetID=b.getUniqueId();
//			WarCraft.l("Effect for "+(long)(fx.getDuration()*1000.0)+"ms");
			runTill = System.currentTimeMillis()+(long)(fx.getDuration()*1000.0);
		}
		
		/*wcEffect getEffect() { return fx; }
		Living getTarget() { return target; }
		UUID getTargetID() { return targetID; }*/
		boolean isTimedout(long now) { return fx.getDuration()>0&&runTill<=now; }
	}
}
