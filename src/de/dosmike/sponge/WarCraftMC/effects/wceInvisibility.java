package de.dosmike.sponge.WarCraftMC.effects;

import java.util.Optional;

import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.Living;

import de.dosmike.sponge.mikestoolbox.living.CustomEffect;

public class wceInvisibility implements WarCraftCustomEffect {

	private final PotionEffect fx;
	private final double duration;
	public wceInvisibility(double duration) {
		this.duration=duration;
		PotionEffect.Builder fcb = PotionEffect.builder(); fcb
		.amplifier(1)
		.duration((int)(Math.ceil(duration)+1)*20) //with some buffer, we strip it later again
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
//		entity.getWorld().playSound(SoundTypes.ENTITY_ENDERMEN_TELEPORT, SoundCategories.MASTER, entity.getLocation().getPosition(), 1.0);
		Optional<PotionEffectData> perhaps = entity.getOrCreate(PotionEffectData.class); 
		if (!perhaps.isPresent()) return;
		PotionEffectData data = perhaps.get();
		data.addElement(fx);
		entity.offer(data);
	}

	@Override
	public void onTick(Living entity, int dt) {
		
	}

	@Override
	public void onRemove(Living entity) {
		Optional<PotionEffectData> perhaps = entity.getOrCreate(PotionEffectData.class); 
		if (!perhaps.isPresent()) return;
		PotionEffectData data = perhaps.get();
		for (PotionEffect e : data.asList()) if (e.getType().equals(fx.getType())) { data.remove(e); break; }
		entity.offer(data);
	}
	
}
