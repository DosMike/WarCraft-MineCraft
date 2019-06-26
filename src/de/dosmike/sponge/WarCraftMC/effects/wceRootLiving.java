package de.dosmike.sponge.WarCraftMC.effects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.entity.living.Living;

import de.dosmike.sponge.mikestoolbox.living.CustomEffect;

public class wceRootLiving implements WarCraftCustomEffect {

	/** special valus that have no other place to go */
	public static Set<UUID> frozenEntities = new HashSet<>();
	
	private final double duration;
	public wceRootLiving(double duration) {
		this.duration=duration;
	}
	
	@Override
	public String getName() {
		return "Root";
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
//		entity.getWorld().playSound(SoundTypes.BLOCK_GLASS_BREAK, SoundCategories.PLAYER, entity.getLocation().getPosition(), 1.0);
//		if (entity instanceof Player)  WarCraft.tell( ((Player) entity), TextColors.AQUA, String.format("You are frozen for %.2fs", duration));
		frozenEntities.add(entity.getUniqueId());
	}

	@Override
	public void onTick(Living entity, int dt) {
		
	}

	@Override
	public void onRemove(Living entity) {
		frozenEntities.remove(entity.getUniqueId());
//		entity.getWorld().playSound(SoundTypes.BLOCK_GLASS_BREAK, SoundCategories.PLAYER, entity.getLocation().getPosition(), 1.0);
//		if (entity instanceof Player)  WarCraft.tell( ((Player) entity), TextColors.AQUA, "You are no longer frozen");
	}
	
}
