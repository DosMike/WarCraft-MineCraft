package de.dosmike.sponge.WarCraftMC.races;

import java.text.ParseException;
import java.util.Optional;

import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.exceptions.ActionBuilderException;
import de.dosmike.sponge.WarCraftMC.exceptions.ExecuteSkillException;
import de.dosmike.sponge.WarCraftMC.exceptions.PrepareSkillActionException;

public class Skill {
	int skillNeeded;
	String name;
	String desc;
	String cooldown;
	String id;
	
	double[][] parameter;
	
	Action[] actions;
	
	/** the required race level before this skill can be unlocked */
	public int getSkillLevel() {
		return skillNeeded;
	}
	public String getName() {
		return name;
	}
	public String getID() {
		return id;
	}
	public String getDescription() {
		return desc;
	}
	/** returns the cooldown for this skill in milliseconds for a certain skilllevel
	 * @param skillLevel replace level in the formula with */
	public long getCooldown(int skillLevel) {
		return (long)(Race.engine.evaluate(cooldown.replace("level", String.valueOf(skillLevel)))*1000);
	}
	/** return the maximum ammount of times you can level this skill */
	public int getMaxSkill() {
		return parameter.length;
	}
	/** return the parameter array for a certain skill level
	 * @param forSkillLevel natural (1-base) for name logic */
	public double[] getParameters(int forSkillLevel) {
		return parameter[forSkillLevel-1];
	}
	
	/** FIRE SHOULD BE CALLED FROM PROFILE TO GENERATE A EVENT! DO NOT CALL<br>
	 * Returns a skill result stripped of all SUCCESS properties for simplicity.<br>
	 * For each action triggered without a SUCCESS property false in it's chain the COOLDOWN property will be added.*/
	public SkillResult fire(ActionData data) throws ExecuteSkillException {
		SkillResult result=new SkillResult();
		try {
			for (Action action : actions) {
				Optional<SkillResult> r = action.fire(data);
				if (r.isPresent()) {
					if (!r.get().get(ResultProperty.SUCCESS).contains(false)) result.push(ResultProperty.COOLDOWN, true);
					result.push(r.get());
				}
			}
		} catch (Exception e) {
			throw new ExecuteSkillException("Unable to cast skill \""+id+"\" with " +data, e);
		}
		return result;
	}
	

	public static class Builder {
		Skill building;
		private Builder(String id) { building = new Skill(id); }
		
		public Builder setName(String name) {
			building.name=name;
//			WarCraft.l(" With Skill: "+name);
			return this;
		}
		public Builder setDescription(String desc) {
			building.desc=desc;
			return this;
		}
		public Builder setCooldown(String cooldown) {
			building.cooldown=cooldown;
			return this;
		}
		public Builder setParameterMap(double[][] parameterMap) {
			building.parameter = parameterMap;
			return this;
		}
		public Builder setSkillNeeded(int skillNeeded) {
			building.skillNeeded = skillNeeded;
			return this;
		}
		public Builder setActions(String... actions) throws ActionBuilderException {
			building.actions = new Action[actions.length];
			for (int i = 0; i < actions.length; i++)
				try {
//					WarCraft.l("  Parsing Action "+(i+1));
					building.actions[i] = Action.fromString(actions[i]);
				} catch (ParseException | PrepareSkillActionException e) {
					throw new ActionBuilderException("Failed to put action "+i+" into Skill "+building.name, e);
				}
			return this;
		}
		public Skill build() { return building; }
	};
	public static Builder builder(String id) {
		return new Builder(id);
	}
	
	Skill(String skillID) { id = skillID.toLowerCase(); if (!id.matches("[\\w]+")) throw new IllegalArgumentException("Skill IDs may only consist of letters, numbers and underscores!"); }
	
	
}
