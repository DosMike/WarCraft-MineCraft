package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;

public class wceDelayedLightning implements wcEffect {

	private final double delay;
	private final double damage;
	private final Cause cause;
	public wceDelayedLightning(double delay, double damage, Cause lightningCause) {
		this.delay = delay;
		this.damage = damage;
		this.cause = lightningCause;
	}
	
	@Override
	public String getName() {
		return "Delayed Lightning";
	}

	@Override
	public double getDuration() {
		return delay;
	}

	@Override
	public boolean isInstant() {
		return false;
	}

	@Override
	public void onApply(Living entity) {
	}

	@Override
	public void onTick(Living entity, int dt) {
	}

	@Override
	public void onRemove(Living entity) {
		Lightning bolt = (Lightning)entity.getWorld().createEntity(EntityTypes.LIGHTNING, entity.getLocation().getPosition());
		bolt.setEffect(true); //no dmg?
		entity.getWorld().spawnEntity(bolt, cause);
		EntityDamageSource causeExtra = EntityDamageSource.builder().entity(cause.first(Player.class).get()).type(DamageTypes.CUSTOM).build();
//		causeExtra.
		entity.damage(damage, causeExtra, Cause.builder().from(cause).suggestNamed("Lightning", causeExtra).build());//apply custom ammount of damage
	}
	
}
