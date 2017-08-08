package de.dosmike.sponge.WarCraftMC.skills;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;

import de.dosmike.sponge.WarCraftMC.ManaPipe;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.wcSkill;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.events.EventCause;

/** skills affecting/maipulating the stats of a living like health, mana, .... */
public class StatusSkills {

	@wcSkill("DrainMana")
	public static SkillResult skillManaDrain(Living source, Double amount) {
		if (source instanceof Player) {
			Player p = (Player) source;
			ManaPipe.subMana(p, amount.intValue());
			return new SkillResult().push(ResultProperty.SUCCESS, true);
		}
		return new SkillResult().push(ResultProperty.SUCCESS, false);
	}

	@wcSkill("setHealth")
		public static SkillResult skillSetHealth(Living source, Double amount) {
	//		if (source instanceof Player) {
				WarCraft.l("Giving "+source+" "+amount+"hp");
				Player p = (Player) source;
				p.offer(Keys.MAX_HEALTH, amount>20.0?amount:20.0); //gets reset on next respawn
				p.offer(Keys.HEALTH, amount);
				return new SkillResult().push(ResultProperty.SUCCESS, true);
	//		}
	//		return new SkillResult().push(ResultProperty.SUCCESS, false);
		}

	@wcSkill("heal")
	public static SkillResult skillGiveHealth(Living source, Double amount) {
		Double value = source.get(Keys.HEALTH).get()+amount;
		Double max = source.get(Keys.MAX_HEALTH).get();
		source.offer(Keys.HEALTH, value>max?max:value);
		
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("hurt")
	public static SkillResult skillTakeHealth(Living target, Double amount, Living source) {
//		Double value = target.get(Keys.HEALTH).get()-amount;
//		target.offer(Keys.HEALTH, value<0.0?0.0:value);
		Cause cause = new EventCause().bake(NamedCause.source(source)).bake(NamedCause.hitTarget(target)).get();
		EntityDamageSource causeExtra = EntityDamageSource.builder().entity(source).type(DamageTypes.CUSTOM).build();
		target.damage(amount, causeExtra, Cause.builder().from(cause).suggestNamed("Skill Hurt", causeExtra).build());//apply custom ammount of damage
		
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

}
