package de.dosmike.sponge.WarCraftMC.effects;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import de.dosmike.sponge.mikestoolbox.living.CustomEffect;
import de.dosmike.sponge.mikestoolbox.tracer.BoxTracer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.util.Color;

/** effect wrapper to BoxTracer to bind this trace to a player as effect */
public class wceTraceLine implements WarCraftCustomEffect {

	final double duration;
	final Vector3d source, target;
	final ParticleEffect colorParticle;
	public wceTraceLine(Vector3d src, Vector3d target, Color color, double duration) {
		this.duration = duration;
		this.source = src;
		this.target = target;
		colorParticle = ParticleEffect.builder()
				.velocity(Vector3d.ZERO)
				.type(ParticleTypes.REDSTONE_DUST)
				.option(ParticleOptions.COLOR, color)
				.build();
	}
	
	@Override
	public String getName() {
		return "Trace Line";
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
		onTick(entity, 0);
	}

	@Override
	public void onTick(Living entity, int dt) {
		BoxTracer.drawTrace(colorParticle, entity.getWorld(), source, target);
	}

	@Override
	public void onRemove(Living entity) {

	}
	
}
