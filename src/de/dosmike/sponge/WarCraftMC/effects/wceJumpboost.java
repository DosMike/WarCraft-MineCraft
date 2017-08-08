package de.dosmike.sponge.WarCraftMC.effects;

import java.util.Optional;

import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.Living;

public class wceJumpboost implements wcEffect {

	private final PotionEffect fx;
	private final double duration;
	public wceJumpboost(double duration, double amount) {
		this.duration=duration;
		PotionEffect.Builder fcb = PotionEffect.builder(); fcb
		.amplifier((int)amount)
		.duration((int)(Math.ceil(duration)+1)*20) //with some buffer, we strip it later again
		//as fas as i understand particles can hide particles and ambient would make visible particles half transparent? 
		.particles(true)
		.potionType(PotionEffectTypes.JUMP_BOOST);
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
		Optional<PotionEffectData> perhaps = entity.getOrCreate(PotionEffectData.class); 
		if (!perhaps.isPresent()) return;
		PotionEffectData data = perhaps.get();
		data.addElement(fx);
		entity.offer(data);
	}

	@Override
	public void onTick(Living entity) {
		
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
