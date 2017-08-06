package de.dosmike.sponge.WarCraftMC.races;

import java.util.List;
import java.util.function.Consumer;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;

public class Race {
	String id, name;
	String description;
	int requiredLevel;
	int startSkill;
	long[] levelXP;
	
	Skill[] skills;
	
	public String getID() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	/** returns the global level required to play this race */
	public int getRequiredLevel() { return requiredLevel; }
	/** The total times you can level up is defined by levelXP and a player usually starts at level 1
	 * thus the max level = levelXP.length()+1 */
	public int getMaxLevel() { return levelXP.length+1; }
	/** returns the xp needed to level up, always 0 if player is at max level, so check that first!
	 * @param level counts natural (1-based) */
	public long getLevelXp(int level) { return (level==getMaxLevel()?0:levelXP[level-1]); }
	/** returns the amount of skills this race has. usually this should not exceed 5 for sake of sanity */
	public int getSkillCount() { return skills.length; }
	/** returns the amount of skillpoints a player starts with in this race */
	public int getStartSkill() { return startSkill; }
	/** counting 0-based*/
	public Skill getSkill(int i) { return skills[i]; }
	public void forEachSkill(Consumer<Skill> consumer) {
		for (Skill skill : skills) consumer.accept(skill);
	}
	
	public static class Builder {
		Race building;
		private Builder(String raceID) {
			building = new Race(raceID);
			WarCraft.l("Adding race: "+raceID);
		}

		public Builder setName(String name) {
			building.name = name;
			return this;
		}
		public Builder setDescription(String desc) {
			building.description = desc;
			return this;
		}
		public Builder setStartSkill(int skillPoints) {
			building.startSkill = skillPoints;
			return this;
		}
		public Builder setRequiredLevel(Integer level) {
			building.requiredLevel = level;
			return this;
		}
		/** The index represents the current level, the value at index are the xp required within the level to level up */
		public Builder setLevelXP(List<Long> list) {
			building.levelXP = new long[list.size()];
			for (int i = 0; i < list.size(); i++)
				building.levelXP[i] = list.get(i);
			return this;
		}
		public Builder setSkills(List<Skill> skills) {
			building.skills = skills.toArray(new Skill[skills.size()]);
			if (building.skills.length>0) for (int s=0;s<building.skills[1].actions.length;s++)
				if (building.skills[0].actions[s].event == Trigger.ACTIVE_INVALID)
					building.skills[0].actions[s].event = Trigger.ACTIVE1;
			if (building.skills.length>1) for (int s=0;s<building.skills[1].actions.length;s++)
				if (building.skills[1].actions[s].event == Trigger.ACTIVE_INVALID)
					building.skills[1].actions[s].event = Trigger.ACTIVE2;
			if (building.skills.length>2) for (int s=0;s<building.skills[1].actions.length;s++)
				if (building.skills[2].actions[s].event == Trigger.ACTIVE_INVALID)
					building.skills[2].actions[s].event = Trigger.ACTIVE3;
			if (building.skills.length>3) for (int s=0;s<building.skills[1].actions.length;s++)
				if (building.skills[3].actions[s].event == Trigger.ACTIVE_INVALID)
					building.skills[3].actions[s].event = Trigger.ULTIMATE;
			if (building.skills.length>5) {
				throw new RuntimeException("A race can't have more than 5 skills, the first 4 might use the active trigger, the 5th has to be passive!");
			}
			return this;
		}
		
		public Race build() { return building; }
	};
	public static Builder builder(String raceID) {
		return new Builder(raceID);
	}
	
	Race(String raceID) { id = raceID.toLowerCase(); if (!id.matches("[\\w]+")) throw new IllegalArgumentException("Race IDs may only consist of letters, numbers and underscores!"); }
}
