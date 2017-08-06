package de.dosmike.sponge.WarCraftMC;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

import de.dosmike.sponge.WarCraftMC.Manager.StatusEffectManager;
import de.dosmike.sponge.WarCraftMC.effects.wceInvisibility;
import de.dosmike.sponge.WarCraftMC.effects.wceRootLiving;

public class Skills {
	static Map<String, Method> autoRegistry = new HashMap<>();
	static { registerSkills(Skills.class); }
	public static void registerSkills(Class<?> skillProvider) { 
		for (Method m : Skills.class.getMethods())
			if (m.isAnnotationPresent(wcSkill.class))
				autoRegistry.put(m.getAnnotation(wcSkill.class).value().toLowerCase(), m);
	}
	public static Method getSkill(String name) {
		Method result = autoRegistry.get(name.toLowerCase());
		if (result == null) throw new RuntimeException("No skill handler for: "+name.toUpperCase());
		return result;
	}
	
	@wcSkill("Blink")
	public static void skillTeleport(Living source, Double distance) {
		BlockRay<World> blockRay = BlockRay.from(source)
//				.skipFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
				.stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
				.distanceLimit(distance).build();
		Optional<BlockRayHit<World>> end = blockRay.end();
		if (end.isPresent()) {
			BlockRayHit<World> hit = end.get();
			Direction d = hit.getFaces()[0];
			source.setLocationSafely(hit.getLocation().getRelative(d));

		}
		source.getWorld().playSound(SoundTypes.ENTITY_BLAZE_SHOOT, SoundCategories.PLAYER, source.getLocation().getPosition(), 1.0);
	}
	@wcSkill("Invisibility")
	public static void skillInvisibility(Living source, Double duration) {
		StatusEffectManager.add(source, new wceInvisibility(duration));
	}
	@wcSkill("DrainMana")
	public static void skillManaDrain(Living source, Double amount) {
		if (source instanceof Player) {
			Player p = (Player) source;
			ManaPipe.subMana(p, amount.intValue());
		}
	}
	@wcSkill("Freeze")
	public static void skillFreeze(Living source, Double duration) {
		StatusEffectManager.add(source, new wceRootLiving(duration));
	}
	@wcSkill("setHealth")
	public static void skillSetHealth(Living source, Double amount) {
		if (source instanceof Player) {
			WarCraft.l("Giving "+source+" "+amount+"hp");
			Player p = (Player) source;
			p.offer(Keys.MAX_HEALTH, amount>20.0?amount:20.0); //gets reset on next respawn
			p.offer(Keys.HEALTH, amount);
		}
	}
	
	/** having SoundTypes is well and OK but not having a playSound(String rawSoundType... is a design flaw<br>
	 * Any resource pack can add additional sounds and while not every player may head that sound - it <i>may</i> be<br>
	 * audible if it's contained in a server resource pack. That's why the /playsound command says it plays a sound<br>
	 * even if it does not exist. */
	@wcSkill("playsound")
	public static void effectPlaysound(Living source, String sound) {
		Optional<SoundType> snd = wcUtils.getTypeByName(sound, SoundTypes.class);
		if (snd.isPresent())
			source.getWorld().playSound(snd.get(), SoundCategories.PLAYER, source.getLocation().getPosition(), 1.0);
	}
	
	
}
