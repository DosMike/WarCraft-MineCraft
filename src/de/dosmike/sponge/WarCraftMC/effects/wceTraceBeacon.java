package de.dosmike.sponge.WarCraftMC.effects;

import com.flowpowered.math.vector.Vector3d;
import de.dosmike.sponge.mikestoolbox.living.CustomEffect;
import de.dosmike.sponge.mikestoolbox.tracer.BoxTracer;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.util.Color;

/** effect wrapper to BoxTracer to bind this trace to a player as effect */
public class wceTraceBeacon implements WarCraftCustomEffect {

	final double duration;
	double radius;
	Vector3d location;
	final double deltaRadius; //in blocks per milli second
	final ParticleEffect colorParticle;
	public wceTraceBeacon(Color color, double fromRadius, double toRadius, double duration) {
		this.duration = duration;
		this.radius = fromRadius;
		this.deltaRadius = (toRadius-fromRadius)/(duration*1000);
		colorParticle = ParticleEffect.builder()
				.velocity(Vector3d.ZERO)
				.type(ParticleTypes.REDSTONE_DUST)
				.option(ParticleOptions.COLOR, color)
				.build();
	}
	
	@Override
	public String getName() {
		return "Trace Beacon";
	}

	@Override
	public double getDuration() {
		return duration;
	}

	@Override
	public boolean isInstant() {
		return duration < 0.1;
	}

	@Override
	public void onApply(Living entity) {
		location = entity.getLocation().getPosition().add(0,1,0);
		onTick(entity, 0);
	}

	@Override
	public void onTick(Living entity, int dt) {
		//shifting is faster than multiplication and since we do that a lot here...
		radius += (dt * deltaRadius);
		if (radius <= 0) return;
		int segments = Math.min(64,(int)(radius * 8f)); //no more than 64 segments
		if (segments < 3) {
			entity.getWorld().spawnParticles(colorParticle, location);
		} else {
			double deltaRad = Math.PI / (segments >>> 1);
			double rad0 = 0;
			for (; segments > 0; segments--) {
				Vector3d segFrom = location.add(Math.cos(rad0) * radius, 0, Math.sin(rad0) * radius);
				rad0 += deltaRad;
				Vector3d segTo = location.add(Math.cos(rad0) * radius, 0, Math.sin(rad0) * radius);
				BoxTracer.drawTrace(colorParticle, entity.getWorld(), segFrom, segTo);
			}
		}
	}

	@Override
	public void onRemove(Living entity) {

	}
	
}
