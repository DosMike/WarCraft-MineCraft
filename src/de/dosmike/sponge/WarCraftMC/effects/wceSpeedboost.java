package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.entity.living.Living;

public class wceSpeedboost implements wcEffect {

//	private final PotionEffect fx;
	private final double duration;
	private final double amount;
	public wceSpeedboost(double duration, double amount) {
		this.duration=duration;
		this.amount=amount/10.0;
	}
	
	@Override
	public String getName() {
		return "Speed Boost";
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
//		Double speed = entity.get(Keys.WALKING_SPEED).orElse(0.1);
//		WarCraft.l("Changing speed from "+speed+" to " +(speed+amount));
//		entity.offer(Keys.WALKING_SPEED, speed+amount);
	}

	@Override
	public void onTick(Living entity) {
		
	}

	@Override
	public void onRemove(Living entity) {
//		Double speed = entity.get(Keys.WALKING_SPEED).orElse(0.1);
//		WarCraft.l("Returning speed from "+speed+" to " +(speed-amount));
//		entity.offer(Keys.WALKING_SPEED, speed-amount);
	}
	
}
