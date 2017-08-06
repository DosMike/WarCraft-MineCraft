package de.dosmike.sponge.WarCraftMC.races;

import java.text.ParseException;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.exceptions.ActionBuilderException;
import de.dosmike.sponge.WarCraftMC.exceptions.ExecuteSkillException;
import de.dosmike.sponge.WarCraftMC.exceptions.PrepareSkillActionException;

public class Skill {
	int skillNeeded;
	String name;
	String desc;
	double cooldown;
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
	public String getDescription() {
		return desc;
	}
	/** returns the cooldown for this skill in seconds */
	public double getCooldown() {
		return cooldown;
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
	
	/** FIRE SHOULD BE CALLED FROM PROFILE TO GENERATE A EVENT! DO NOT CALL
	 * returns true if one or more actions were triggered, indicating this skill was used and should now be on cooldown */
	public boolean fire(ActionData data) throws ExecuteSkillException {
		boolean r = false;
		try {
			for (Action action : actions) {
				if (action.fire(data))r=true;
			}
		} catch (Exception e) {
			throw new ExecuteSkillException("Unable to cast skill \""+name+"\" with " +data, e);
		}
		return r;
	}
	

	public static class Builder {
		Skill building;
		private Builder(String id) { building = new Skill(id); }
		
		public Builder setName(String name) {
			building.name=name;
			WarCraft.l(" With Skill: "+name);
			return this;
		}
		public Builder setDescription(String desc) {
			building.desc=desc;
			return this;
		}
		public Builder setCooldown(double cooldown) {
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
		public Builder setActions(String... actions) {
			building.actions = new Action[actions.length];
			for (int i = 0; i < actions.length; i++)
				try {
					WarCraft.l("  Parsing Action "+(i+1));
					building.actions[i] = Action.fromString(actions[i]);
				} catch (ParseException | PrepareSkillActionException e) {
					new ActionBuilderException("Failed to put action "+i+" into Skill "+building.name, e).printStackTrace();
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
