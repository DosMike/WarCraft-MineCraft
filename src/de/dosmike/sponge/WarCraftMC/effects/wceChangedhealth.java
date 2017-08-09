package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;

public class wceChangedhealth implements wcEffect {

	final double health;
	public wceChangedhealth(double amount) {
		health = amount;
	}
	
	@Override
	public String getName() {
		return "Invisibility";
	}

	@Override
	public double getDuration() {
		return 0.0;
	}

	@Override
	public boolean isInstant() {
		return false;
	}

	@Override
	public void onApply(Living entity) {
		entity.offer(Keys.MAX_HEALTH, health>20.0?health:20.0); //gets reset on next respawn
		entity.offer(Keys.HEALTH, health);
	}

	@Override
	public void onTick(Living entity, int dt) {
		
	}

	@Override
	public void onRemove(Living entity) {
		entity.offer(Keys.MAX_HEALTH, 20.0); //leaving wc / respawning / etc
	}
	
}
