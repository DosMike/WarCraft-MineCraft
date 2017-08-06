package de.dosmike.sponge.WarCraftMC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.NamedCause;

import com.google.common.reflect.TypeToken;

import de.dosmike.sponge.WarCraftMC.events.EventCause;
import de.dosmike.sponge.WarCraftMC.events.UseSkillEvent;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.WarCraftMC.races.Skill;
import ninja.leaping.configurate.ConfigurationNode;

public class RaceData {
	Race race;
	
	int[] skillProgress;
	int skillPoints; //skillPoints the player can spend on skillProgress
	int raceLevel;
	long xp; //xp the player has within this race
	long[] skillCooldown; //timestamp until skill is on cooldown
	
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
		xp -= race.getLevelXp(raceLevel);
		if (xp<0)xp=0;
		raceLevel++;
		skillPoints++; //make the amount of skillpoints per level a config value? this would mess with the perk system tho
	}

	public long getXP() {
		return xp;
	}
	/** <b>DO NOT CALL</b> calling this directly will ignore all events!<br>
	 * Use Profile.pushXP instead!
	 * @param return how many levels the player could raise now */
	public int giveXP(long xp) {
		this.xp += xp;
		
		int levels = 0;
		long test = this.xp;
		while ((raceLevel+levels)<race.getMaxLevel() && test >= race.getLevelXp(raceLevel+levels)) { test -= race.getLevelXp(raceLevel+levels); levels++; }
		return levels;
	}
	/** required for xp leech
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
		for (int i=0;i<result.skillCooldown.length;i++) result.skillCooldown[i]=System.currentTimeMillis()+(long)(race.getSkill(i).getCooldown()*1000);
		
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
	
	/** fire these actionData for each skill */
	public void fire(Profile link, ActionData baseData) {
		if (!link.isActive()) return;
		EventCause cause = new EventCause(Sponge.getServer().getPlayer(link.getPlayerID()).get());
		if (baseData.getTarget().isPresent()) cause.bake(NamedCause.HIT_TARGET, baseData.getTarget().get());
		long now = System.currentTimeMillis();
		for (int i=0; i < getRace().getSkillCount(); i++) {
			if (skillProgress[i]<=0) continue;
			if (skillCooldown[i]> now) continue;
			Skill skill = getRace().getSkill(i);
//			WarCraft.l("Go: "+skill.getName());

			UseSkillEvent event = new UseSkillEvent(link, skill, baseData, cause.get());
			/* copy this */Sponge.getEventManager().post(event); if (event.isCancelled()) continue;/* pasta that */
			try {
				if (skill.fire(
					ActionData.builder(baseData).setParameters(skill.getParameters(skillProgress[i])).build()
					))
					skillCooldown[i]=System.currentTimeMillis()+(long)(skill.getCooldown()*1000);
			} catch (Exception e) {
				throw new RuntimeException("Race \""+race.getID().toUpperCase()+"\" seems to be broken:", e);
			}
		}
	}
}
