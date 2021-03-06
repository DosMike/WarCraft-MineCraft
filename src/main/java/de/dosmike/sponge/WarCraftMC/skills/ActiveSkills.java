package de.dosmike.sponge.WarCraftMC.skills;

import de.dosmike.sponge.WarCraftMC.Manager.SkillManager;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.effects.*;
import de.dosmike.sponge.WarCraftMC.wcSkill;
import de.dosmike.sponge.mikestoolbox.living.BoxLiving;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.*;

public class ActiveSkills {

	@wcSkill("Blink")
		public static SkillResult skillTeleport(Living source, Double distance) {
			BlockRay<World> blockRay = BlockRay.from(source)
	//				.skipFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
					.stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
					.distanceLimit(distance).build();
			Optional<BlockRayHit<World>> end = blockRay.end();
	//		if (end.isPresent()) {
				BlockRayHit<World> hit = end.get();
				Direction d = hit.getFaces()[0];
				source.setLocationSafely(hit.getLocation().getRelative(d));
				source.getWorld().playSound(SoundTypes.ENTITY_BLAZE_SHOOT, SoundCategories.PLAYER, source.getLocation().getPosition(), 1.0);
				return new SkillResult().push(ResultProperty.SUCCESS, true);
	//		}
	//		return new SkillResult().push(ResultProperty.SUCCESS, false);
		}

	@wcSkill("Invisibility")
	public static SkillResult skillInvisibility(Living source, Double duration) {
		BoxLiving.addCustomEffect(source, new wceInvisibility(duration));
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("Freeze")
	public static SkillResult skillFreeze(Living source, Double duration) {
		BoxLiving.addCustomEffect(source, new wceRootLiving(duration));
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("Ignite")
	public static SkillResult skillIgnite(Living target, Double seconds) {
		target.offer(Keys.FIRE_TICKS, (int)(seconds*20));
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("slowHeal")
	public static SkillResult skillRegenerateHealth(Living target, Integer healthPoints) {
		BoxLiving.addCustomEffect(target, new wceSlowHeal(healthPoints));
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("bonusDamage")
	public static SkillResult skillBonusDamage(Double amount) {
		return new SkillResult().push(ResultProperty.MODIFY_DAMAGE, amount).push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("cancelEvent")
	public static SkillResult skillCancelEvent() {
		return new SkillResult().push(ResultProperty.CANCEL_ACTION, true).push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("explosion")
	public static SkillResult skillExplode(Living entity, Double amplitude) {
		entity.getWorld().triggerExplosion(Explosion.builder()
				.location(entity.getLocation())
				.radius(amplitude.floatValue())
				.shouldBreakBlocks(false)
				.shouldDamageEntities(true)
				.shouldPlaySmoke(false)
				.canCauseFire(false)
//				.build(), new EventCause().bake(NamedCause.source(entity)).get());
				.build());
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("chainlightning")
	public static SkillResult skillLighning(Living source, Double targets, Double range, Double damage) {
		if (!(source instanceof Player)) return new SkillResult().push(ResultProperty.SUCCESS, false);
		Player player = (Player)source;
		List<Entity> nearby = new ArrayList<>();
		nearby.addAll(source.getNearbyEntities(range));
		nearby.removeIf(((t) -> {
				if (!(t instanceof Living)) return true;
				if (t.equals(source)) return true; //don't fire on self
				Living l = (Living)t;
				for (Team team : player.getScoreboard().getTeams()) { //prevent team attacks
					if (!team.allowFriendlyFire() && team.getMembers().contains(l.getTeamRepresentation())) return true;
				}
				return false;
		})); 
		Collections.shuffle(nearby);
		for (int i=0; i<nearby.size()&&i<targets; i++) {
//			StatusEffectManager.add((Living)nearby.get(i), new wceDelayedLightning((double)(i+1)*0.3, damage, new EventCause((Player)source).bake(NamedCause.hitTarget(nearby.get(i))).get()));
			BoxLiving.addCustomEffect((Living)nearby.get(i), new wceDelayedLightning((double)(i+1)*0.3, damage, Sponge.getCauseStackManager().getCurrentCause()));
		}
		
		return new SkillResult().push(ResultProperty.SUCCESS, !nearby.isEmpty());
	}

	@wcSkill("areafreeze")
	public static SkillResult skillAreaFreeze(Living source, Double range, Double duration) {
		List<Entity> nearby = new LinkedList<>();
		nearby.addAll(source.getNearbyEntities(range));
		nearby.remove(source);
		for (Entity e : nearby) if (e instanceof Living) BoxLiving.addCustomEffect((Living)e, new wceRootLiving(duration));
		
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("speedboost")
	public static SkillResult skillSpeedboost(Living entity, Double duration, Double amount) {
		BoxLiving.addCustomEffect(entity, new wceSpeedboost(duration, amount));
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("jumpboost")
	public static SkillResult skillJumpboost(Living entity, Double duration, Double amount) {
		BoxLiving.addCustomEffect(entity, new wceJumpboost(duration, amount));
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}
	
	@wcSkill("confuse")
	public static SkillResult skillConfuse(Living entity, Double duration, Double amount) {
		BoxLiving.addCustomEffect(entity, new wceConfusion(duration, amount));
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}
	
	@wcSkill("thrownade") //Fireballs are not shootable, use sub interface (see jdocs)
	public static SkillResult skillFireball(Living source, Double speed, Double damage) {
		if (!(source instanceof Player)) return new SkillResult().push(ResultProperty.SUCCESS, true);
//		Optional<LargeFireball> maybe = ((Player)source).launchProjectile(LargeFireball.class, source.getHeadRotation().mul(speed/100.0));
//		if (!maybe.isPresent()) {
//			if (source instanceof Player) WarCraft.tell((Player)source, "FIREBALL! ... looks like this is not yet implemented :<");
//			return new SkillResult().push(ResultProperty.SUCCESS, false);
//		}
//		LargeFireball fireball = maybe.get();
//		fireball.setVelocity(source.getHeadRotation().mul(speed/100.0));
////		fireball.offer(Keys.HAS_GRAVITY, true);
		
		SkillManager.thorwNade(source, speed, damage.floatValue());
		
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

}
