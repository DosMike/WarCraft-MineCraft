package de.dosmike.sponge.WarCraftMC.effects;

import de.dosmike.sponge.mikestoolbox.living.CustomEffect;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;

/** Slowly heal 1 hp every half second over X hp */
public class wceSlowHeal implements WarCraftCustomEffect {

	private double amount;
	private double duration;
	private int timeCount;

	/**
	 * @param amount amount of healthpoints to heal */
	public wceSlowHeal(int amount) {
		this.amount = amount;
		this.duration = amount*10+5;
		this.timeCount = 0;
	}
	
	@Override
	public String getName() {
		return "Slow Heal";
	}

	@Override
	public double getDuration() {
		return duration;
	}

	@Override
	public boolean isRunning() {
		return amount > 0;
	}
	
	@Override
	public void onTick(Living entity, int dt) {
		timeCount += dt;
		while (timeCount > 500 && amount > 0) {
			timeCount -= 500;
			amount--;
			Double health = entity.get(Keys.HEALTH).get();
			entity.offer(Keys.HEALTH, health+1);
		}
	}

	
}
