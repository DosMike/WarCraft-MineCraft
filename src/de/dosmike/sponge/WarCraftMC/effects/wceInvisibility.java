package de.dosmike.sponge.WarCraftMC.effects;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.Living;

import de.dosmike.sponge.WarCraftMC.WarCraft;

public class wceInvisibility implements wcEffect {

	private final PotionEffect fx;
	private final double duration;
	public wceInvisibility(double duration) {
		this.duration=duration;
		PotionEffect.Builder fcb = PotionEffect.builder(); fcb
		.amplifier(1)
		.duration((int)(Math.ceil(duration))*20)
		//as fas as i understand particles can hide particles and ambient would make visible particles half transparent? 
		.particles(true)
		.potionType(PotionEffectTypes.INVISIBILITY);
		fx = fcb.build();
	}
	
	@Override
	public String getName() {
		return "Invisibility";
	}

	@Override
	public double getDuration() {
		return duration;
	}

	@Override
	public boolean isInstant() {
		return false;
	}

	@Override
	public void onApply(Living entity) {
//		WarCraft.l(entity + " hidden for " + duration+"s");
		entity.getWorld().playSound(SoundTypes.ENTITY_ENDERMEN_TELEPORT, SoundCategories.MASTER, entity.getLocation().getPosition(), 1.0);
		Optional<List<PotionEffect>> perhaps = entity.get(Keys.POTION_EFFECTS);
		if (!perhaps.isPresent()) return;
		List<PotionEffect> data = perhaps.get();
		data.add(fx);
		entity.offer(Keys.POTION_EFFECTS, data);
	}

	@Override
	public void onTick(Living entity) {
		
	}

	@Override
	public void onRemove(Living entity) {
		Optional<List<PotionEffect>> perhaps = entity.get(Keys.POTION_EFFECTS);
		if (!perhaps.isPresent()) return;
		List<PotionEffect> data = perhaps.get();
		for (PotionEffect e : data) if (e.getType().equals(fx.getType())) { data.remove(e); break; }
		entity.offer(Keys.POTION_EFFECTS, data);
	}
	
}
