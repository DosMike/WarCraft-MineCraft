package de.dosmike.sponge.WarCraftMC.skills;

import java.util.Optional;

import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.wcSkill;
import de.dosmike.sponge.WarCraftMC.wcUtils;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;

public class SkillEffects {

	@wcSkill("playsound")
	public static SkillResult effectPlaysound(Living source, String sound) {
		Optional<SoundType> snd = wcUtils.getTypeByName(sound, SoundTypes.class);
		if (snd.isPresent())
			source.getWorld().playSound(snd.get(), SoundCategories.PLAYER, source.getLocation().getPosition(), 1.0);
		else
			source.getWorld().playSound(SoundType.builder().build(sound), source.getLocation().getPosition(), 1.0);
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("tell")
	public static SkillResult effectTell(Living target, String message) {
		if (!(target instanceof Player)) return new SkillResult().push(ResultProperty.SUCCESS, false);
		WarCraft.tell((Player)target, message);
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}
	
	@wcSkill("broadcast")
	public static SkillResult effectTell(String message) {
		WarCraft.tell(message);
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}
	
}
