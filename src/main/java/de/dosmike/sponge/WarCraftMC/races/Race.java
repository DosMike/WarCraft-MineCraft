package de.dosmike.sponge.WarCraftMC.races;

import com.dolhub.tech.MathEval;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.languageservice.API.Localized;
import org.spongepowered.api.command.CommandSource;

import java.util.List;
import java.util.function.Consumer;

public class Race {
	static MathEval engine = new MathEval();

	private String id, name;
	private String description;
	private int requiredLevel;
	private int startSkill;
	private int autoMaxLevel;
	private String levelXP;

	private Skill[] skills;
	
	public String getID() { return id; }
	public Localized<String> getName() {
		return WarCraft.T().local(name);
	}
	public String getName(CommandSource player) {
		return WarCraft.T().local(name).orLiteral(player);
	}
	public Localized<String>  getDescription() {
		return WarCraft.T().local(description);
	}
	public String getDescription(CommandSource player) {
		return WarCraft.T().local(description).orLiteral(player);
	}
	/** returns the global level required to play this race */
	public int getRequiredLevel() { return requiredLevel; }
	/** This value is automatically generated in the builder on setSkills<br>
	 * and set to the sum of the maxSkillLevel for each skill 
	 * @return The total times you can level up */
	public int getMaxLevel() { return autoMaxLevel; }
	/** returns the xp needed to level up, always 0 if player is at max level, so check that first!
	 * @param level counts natural (1-based) */
	public long getLevelXp(int level) { return (long)engine.evaluate(levelXP.replace("level", String.valueOf(level))); }
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
//		public Builder setLevelXP(List<Long> list) {
//			building.levelXP = new long[list.size()];
//			for (int i = 0; i < list.size(); i++)
//				building.levelXP[i] = list.get(i);
//			return this;
//		}
		public Builder setLevelXP(String formula) {
			building.levelXP=formula;
			return this;
		}
		public Builder setSkills(List<Skill> skills) {
			building.skills = skills.toArray(new Skill[skills.size()]);
			if (building.skills.length>0) for (int s=0;s<building.skills[0].getActionCount();s++)
				if (building.skills[0].getAction(s).event == Trigger.ACTIVE_INVALID) {
					building.skills[0].getAction(s).event = Trigger.ACTIVE1;
					building.skills[0].appendDescription(" Command: /ability1");
				}
			if (building.skills.length>1) for (int s=0;s<building.skills[1].getActionCount();s++)
				if (building.skills[1].getAction(s).event == Trigger.ACTIVE_INVALID) {
					building.skills[1].getAction(s).event = Trigger.ACTIVE2;
					building.skills[1].appendDescription(" Command: /ability2");
				}
			if (building.skills.length>2) for (int s=0;s<building.skills[2].getActionCount();s++)
				if (building.skills[2].getAction(s).event == Trigger.ACTIVE_INVALID) {
					building.skills[2].getAction(s).event = Trigger.ACTIVE3;
					building.skills[2].appendDescription(" Command: /ability3");
				}
			if (building.skills.length>3) for (int s=0;s<building.skills[3].getActionCount();s++)
				if (building.skills[3].getAction(s).event == Trigger.ACTIVE_INVALID) {
					building.skills[3].getAction(s).event = Trigger.ULTIMATE;
					building.skills[3].appendDescription(" Command: /ultimate");
				}
			if (building.skills.length>5) {
				throw new RuntimeException("A race can't have more than 5 skills, the first 4 might use the active trigger, the 5th has to be passive!");
			}
			building.autoMaxLevel=0;
			for (Skill s : building.skills) building.autoMaxLevel += s.getMaxSkill();
			return this;
		}
		
		public Race build() { return building; }
	};
	public static Builder builder(String raceID) {
		return new Builder(raceID);
	}
	
	Race(String raceID) { id = raceID.toLowerCase(); if (!id.matches("[\\w]+")) throw new IllegalArgumentException("Race IDs may only consist of letters, numbers and underscores!"); }
}
