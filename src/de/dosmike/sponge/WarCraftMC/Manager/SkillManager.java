package de.dosmike.sponge.WarCraftMC.Manager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.explosion.Explosion;

import com.flowpowered.math.vector.Vector3d;

import de.dosmike.sponge.WarCraftMC.wcSkill;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.events.EventCause;
import de.dosmike.sponge.WarCraftMC.skills.ActiveSkills;
import de.dosmike.sponge.WarCraftMC.skills.SkillEffects;
import de.dosmike.sponge.WarCraftMC.skills.StatusSkills;

public class SkillManager {
	static Map<String, Method> autoRegistry = new HashMap<>();
	//Register default skills
	static { 
		registerSkills(SkillManager.class); //usually holds wip skills 
		registerSkills(ActiveSkills.class); 
		registerSkills(StatusSkills.class); 
		registerSkills(SkillEffects.class); 
//		WarCraft.l("Loaded Skill handler: "+Arrays.toString(autoRegistry.keySet().toArray()));
	}
	public static void registerSkills(Class<?> skillProvider) { 
		for (Method m : skillProvider.getDeclaredMethods()) {
			if (m.isAnnotationPresent(wcSkill.class))
				if (!m.getReturnType().equals(SkillResult.class)) {
					throw new RuntimeException("Illegal Skill Action '"+m.getAnnotation(wcSkill.class).value().toLowerCase()+"' needs returntype SkillResult, has " + m.getReturnType().getName());
				} else {
					autoRegistry.put(m.getAnnotation(wcSkill.class).value().toLowerCase(), m);
				}
		}
	}
	public static Method getSkill(String name) {
		Method result = autoRegistry.get(name.toLowerCase());
		if (result == null) throw new RuntimeException("No skill handler for: "+name.toUpperCase());
		return result;
	}
	
	
	/** tracking for explosive charges, mapping the entity to expiration time.<br>
	 * as a nade expires we'll create a explosion there */
	public static class NadeData {
		long expires;
		UUID owner;
		float range;
		public NadeData(UUID owner, long expiresAt, float explosionAmplitude) {
			this.owner=owner;
			expires=expiresAt;
			range=explosionAmplitude;
		}
	}
	
	public static Map<Item, NadeData> nades = new HashMap<>();
	public static void thorwNade(Living source, double speed, float explosionRadius) {
		Vector3d dir = pyr2xyz(source.getHeadRotation());
		Item item = (Item) source.getWorld().createEntity(EntityTypes.ITEM, source.getLocation().getPosition().add(0.0, 1.62, 0.0));
		item.offer(Keys.REPRESENTED_ITEM, 
				ItemStack.builder()
				.itemType(ItemTypes.SLIME_BALL)
				.build().createSnapshot());
		item.setCreator(source.getUniqueId());
		item.offer(Keys.EXPIRATION_TICKS, 100);
		item.offer(Keys.INVULNERABILITY_TICKS, 100);
		
		nades.put(item, new NadeData(source.getUniqueId(), System.currentTimeMillis()+3000, explosionRadius));
		source.getWorld().spawnEntity(item, new EventCause().bake(NamedCause.THROWER, source).get());
		item.setVelocity(dir.mul(speed));
		
	}
	public static void nadeTick() {
		long now = System.currentTimeMillis();
		Set<Item> expired = new HashSet<>();
		for (Entry<Item, NadeData> e : nades.entrySet()) {
			NadeData n = e.getValue();
			if (n.expires<=now) {
				Item nade = e.getKey();
				expired.add(nade);
				nade.remove();
				Optional<Player> thrower = Sponge.getServer().getPlayer(n.owner); //could have disconnected by now
				
				// PLEASE ONLY CREATE EXPLOSIONS LIKE THIS
				//creating a source entity here so we later get a EntityDamageSource and damage for this can be accounted
				//if not we just get a DamageSource instead of a EntityDamageSource that does not hold a creator and thus
				//we won't be able to detect a attacker in our DamageEntityEvent - and that sux
				//Note that we do not have to place the Explosive, we just need to create it as placeholder for the
				//explosions creator
				Explosive ex = (Explosive) nade.getWorld().createEntity(EntityTypes.PRIMED_TNT, nade.getLocation().getPosition());
					if (thrower.isPresent()) ex.setCreator(thrower.get().getUniqueId());
				nade.getWorld().triggerExplosion(
						Explosion.builder()
							.sourceExplosive(ex)
							.canCauseFire(false)
							.location(nade.getLocation())
							.radius(n.range)
							.shouldBreakBlocks(false)
							.shouldDamageEntities(true)
							.shouldPlaySmoke(true)
							.build(),
						(thrower.isPresent()?new EventCause(thrower.get()):new EventCause()).get()
						);
				
			}
		}
		for (Item exploded : expired) nades.remove(exploded);
	}
	
	/** turn head rotation into a normalized xyz representation (direction) */
	private static Vector3d pyr2xyz(Vector3d rotation) {
		
		double pitch = Math.toRadians(rotation.getX()), yaw = Math.toRadians(rotation.getY());
		double x=0.0,y=0.0,z=0.0;
		
		x = -Math.sin(yaw)*Math.cos(pitch);
		y = -Math.sin(pitch);
		z = Math.cos(yaw)*Math.cos(pitch);
		
		return new Vector3d(x,y,z).normalize();
	}
}
