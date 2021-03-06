package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;

public class wceSpeedboost implements WarCraftCustomEffect {

//	private final PotionEffect fx;
	private final double duration;
	private final double amount;
	public wceSpeedboost(double duration, double amount) {
		this.duration=duration;
		this.amount=amount/10.0; //get amount in percent, 100% = 0.1, so divide by 10 again 
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
		Double speed = entity.get(Keys.WALKING_SPEED).orElse(0.1);
//		WarCraft.l("Changing speed from "+speed+" to " +(speed+amount));
		entity.offer(Keys.WALKING_SPEED, speed+amount);
	}

	@Override
	public void onTick(Living entity, int dt) {
		
	}

	@Override
	public void onRemove(Living entity) {
		Double speed = entity.get(Keys.WALKING_SPEED).orElse(0.1);
//		WarCraft.l("Returning speed from "+speed+" to " +(speed-amount));
		entity.offer(Keys.WALKING_SPEED, speed-amount);
	}
	
}
