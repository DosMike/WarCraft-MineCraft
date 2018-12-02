package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;

import de.dosmike.sponge.mikestoolbox.living.CustomEffect;

public class wceChangedhealth implements CustomEffect {

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
		//I won't do this as this results in a unfair healing, when the player respawns/rejoins with this effect 
//		double scaled = entity.get(Keys.HEALTH).orElse(20.0);
//		scaled = scaled*health/20.0; 
//		entity.offer(Keys.HEALTH, scaled);
	}

	@Override
	public void onTick(Living entity, int dt) {
		
	}

	@Override
	public void onRemove(Living entity) {
		entity.offer(Keys.MAX_HEALTH, 20.0); //leaving wc / respawning / etc
	}
	
}
