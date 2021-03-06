package de.dosmike.sponge.WarCraftMC;

import com.google.common.reflect.TypeToken;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.events.UseSkillEvent;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.WarCraftMC.races.Skill;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RaceData {
	Race race;
	
	int[] skillProgress;
	int skillPoints; //skillPoints the player can spend on skillProgress
	int raceLevel;
	long xp; //xp the player has within this race
	long[] skillCooldown; //timestamp until skill is on cooldown
	long levelXP=-1;
	/** returns a cached amount required to level up to reduce calculations */
	public long getLevelXp() {
		return (levelXP<0?(levelXP=race.getLevelXp(raceLevel)):levelXP);
	}
	
	public Race getRace() {
		return race;
	}
	
	/** counting 0-based */
	public int getSkillProgress(int skill) {
		return skillProgress[skill];
	}
	/** the total skill points this player can spend on skill progress */
	public int getSkillPoints() {
		return skillPoints;
	}
	/** return the players level within this race */
	public int getLevel() {
		return raceLevel;
	}
	/** grand levels and skill points
	 * @param takeXP set to false to not take xp in case a admin command want's to give bonus levels or something*/
	public void levelUp(boolean takeXP) {
		if (raceLevel >= race.getMaxLevel()) return;
		xp -= getLevelXp();
		levelXP=-1;// levelXp has to be updated
		if (xp<0)xp=0;
		raceLevel++;
		skillPoints++; //make the amount of skillpoints per level a config value? this would mess with the perk system tho
	}
	/**
	 * reduce race level by one, if not already 1. this DOES NOT re-add the XP to the balance.<br>
	 * If xp in level are not scaled: with increasing XP formulas the remaining XP might
	 * still exceed the required XP and a level up might occur at any moment.<br>
	 * SkillPoints are not touched as they might be spent and it's unclear what skill to reset
	 * @param scaleXP set to true if you want to keep current level progression in percent.
	 *                otherwise the XP will not be scaled
	 * @return the XP the were consumed from the levelup that's being undone
	 */
	public long levelDown(boolean scaleXP) {
		if (raceLevel<=1) return 0; // already at min level
		double progress = (double)getXP()/getLevelXp();
		raceLevel--; //reduce player level
		levelXP=-1; //notify getLevelXp() to recalculate levelXP
		getLevelXp(); //recalculate levelXP
		if (scaleXP) xp = (long)(progress * levelXP);
		return levelXP;
	}

	public long getXP() {
		return xp;
	}
	/** <b>DO NOT CALL</b> calling this directly will ignore all events!<br>
	 * Use Profile.pushXP instead!
	 * @return how many levels the player could raise now */
	public int giveXP(long xp) {
		this.xp += xp;
		
		int levels = 0;
		long test = this.xp; long tmp;
		while ((raceLevel+levels)<race.getMaxLevel() && test >= (tmp=race.getLevelXp(raceLevel+levels))) { test -= tmp; levels++; }
		return levels;
	}
	/** required for xp leech, can't take more xp than in current level
	 * @return the actual amount of taken XP */
	public long takeXP(long xp) {
		this.xp -= xp;
		if (this.xp<0) { xp+=this.xp; this.xp=0; }
		return xp;
	}
	public void spendSkill(int skillNo) {
		if (skillNo<0 || skillNo>=skillProgress.length) throw new IllegalArgumentException("Skill index out of bounds");
		if (skillPoints > 0) { skillProgress[skillNo]++; skillPoints--; }
	}
	public void resetSkill(int skillNo) {
		if (skillNo<0 || skillNo>=skillProgress.length) throw new IllegalArgumentException("Skill index out of bounds");
		skillPoints += skillProgress[skillNo]; skillProgress[skillNo] = 0;
	}
	/** allows negative values (not advised) */
	public void addSkill(int skillPoints) {
		this.skillPoints += skillPoints;
	}
	
	/** called when a connected player switches race or the player profile is loaded.<br>
	 * This class has no serializer in favor of later changes to e.g. a database.<br>
	 * Expects cfg to be the <pre>profile > races</pre> node */
	public static RaceData loadOrCreate(Race race, ConfigurationNode cfg) {
		RaceData result = new RaceData();
		result.race = race;
		result.skillProgress = new int[race.getSkillCount()];
		result.skillCooldown = new long[race.getSkillCount()];
		try {
			ConfigurationNode root = cfg.getNode(race.getID());
			result.skillProgress = toArray(root.getNode("skillProgress").getValue(ttli), new int[race.getSkillCount()]);
			result.skillPoints = root.getNode("skillPoints").getInt(race.getStartSkill());
			result.raceLevel = root.getNode("raceLevel").getInt(1);
			result.xp = root.getNode("xp").getLong(0);
		} catch (Exception e) {
			Arrays.fill(result.skillProgress, 0);
			result.skillPoints=0;
			result.xp=0;
			result.raceLevel=1;
		}
		for (int i=0;i<result.skillCooldown.length;i++) result.skillCooldown[i]=System.currentTimeMillis()+(long)(race.getSkill(i).getCooldown(result.skillProgress[i]));
		return result;
	}
	 /** called when the player switches races or disconnects or the plugin will be halted <br>
	 * Expects cfg to be the <pre>profile > races</pre> node */
	public void Save(ConfigurationNode cfg) {
		try {
			ConfigurationNode root = cfg.getNode(race.getID());
			
			root.getNode("skillProgress").setValue(ttli, toList(skillProgress));
			root.getNode("skillPoints").setValue(skillPoints);
			root.getNode("raceLevel").setValue(raceLevel);
			root.getNode("xp").setValue(xp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// For serialization //
	@SuppressWarnings("serial")
	static TypeToken<List<Integer>> ttli = new TypeToken<List<Integer>>(){};
	private static int[] toArray(List<Integer> list, int[] fallback) {
		try { int[] res = new int[list.size()]; for (int i=0;i<list.size();i++) res[i]=list.get(i); return res; } catch (Exception e) { return fallback; }
	}
	private static List<Integer> toList(int[] list) {
		List<Integer> result = new ArrayList<>(); for (int i : list) result.add(i); return result;
	}
	
	/** Returns a SkillResult if the profile is active containing properties for all fired skills */
	public Optional<SkillResult> fire(Profile link, ActionData baseData) {
		if (!Profile.isActive(Sponge.getServer().getPlayer(link.playerID).get(), link)) return Optional.empty();
//		Player p = Sponge.getServer().getPlayer(link.getPlayerID()).get();
//		EventCause cause = new EventCause(p);
//		if (baseData.getTarget().isPresent()) cause.bake(NamedCause.HIT_TARGET, baseData.getTarget().get());
		long now = System.currentTimeMillis();
		SkillResult result = new SkillResult();
		for (int i=0; i < getRace().getSkillCount(); i++) {
			if (skillProgress[i]<=0) continue;
			Skill skill = getRace().getSkill(i);
			if (skillCooldown[i]> now) continue;
//			WarCraft.l("Go: "+skill.getName());

			UseSkillEvent event = new UseSkillEvent(link, skill, baseData);
			/* copy this */Sponge.getEventManager().post(event); if (event.isCancelled()) continue;/* pasta that */
			try {
				SkillResult tmp = skill.fire(
					ActionData.builder(baseData)
						.setParameters(skill.getParameters(skillProgress[i]))
						.setOnCooldown(skillCooldown[i]>now)
						.build()
					);
				if (tmp.get(ResultProperty.COOLDOWN).contains(true))
					skillCooldown[i]=System.currentTimeMillis()+ skill.getCooldown(skillProgress[i]); //(long)(skill.getCooldown()*1000);
				result.push(tmp);
			} catch (Exception e) {
//				e.printStackTrace();
				StringBuilder simpleTrace = new StringBuilder("Race \""+race.getID().toUpperCase()+"\" seems to be broken:");
				wcUtils.superSimpleTrace(simpleTrace, e, "\n");
				System.err.println(simpleTrace.toString());
			}
		}
		return Optional.of(result);
	}

	/** reset the data in this race to the default values */
	public void reset() {
		Arrays.fill(skillProgress, 0);
		skillPoints=0;
		xp=0;
		raceLevel=1;
		for (int i=0;i<skillCooldown.length;i++) skillCooldown[i]=System.currentTimeMillis()+(long)(race.getSkill(i).getCooldown(skillProgress[i]));
	}
}
