package de.dosmike.sponge.WarCraftMC.races;

import java.util.Arrays;
import java.util.Optional;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;

/** this class shall sanitize the function calls for Skill.fire() and following */
public class ActionData {
	Trigger trigger;
	Optional<Player> self=Optional.empty();
	Optional<Living> target=Optional.empty();
	Optional<ItemStack> item=Optional.empty();
	Optional<Double> damage=Optional.empty();
	double[] parammap=new double[0];
	boolean cooldown=false;
	
	public static Builder builder(Trigger trigger) {
		return new Builder(trigger);
	}
	public static Builder builder(ActionData from) {
		return new Builder(from);
	}
	public static class Builder {
		private ActionData building;
		private Builder(Trigger trigger) {
			building = new ActionData();
			building.trigger=trigger;
		}
		private Builder(ActionData from) {
			building = new ActionData();
			building.trigger=from.trigger;
			building.self=from.self;
			building.target=from.target;
			building.item=from.item;
			building.damage=from.damage;
			building.parammap=from.parammap;
		}
		public Builder setSelf(Player self) {
			building.self=Optional.of(self);
			return this;
		}
		public Builder setOpponent(Living target) {
			building.target=Optional.of(target);
			return this;
		}
		public Builder setItem(ItemStack item) {
			building.item=Optional.of(item);
			return this;
		}
		public Builder setDamage(Double damage) {
			building.damage=Optional.of(damage);
			return this;
		}
		public Builder setParameters(double[] parameter) {
			building.parammap=parameter;
			return this;
		}
		public Builder setOnCooldown(boolean oncooldown) {
			building.cooldown=oncooldown;
			return this;
		}
		public ActionData build() {
			return building;
		}
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public Optional<Player> getSource() {
		return self;
	}

	public Optional<Living> getTarget() {
		return target;
	}

	public Optional<ItemStack> getItem() {
		return item;
	}

	public Optional<Double> getDamage() {
		return damage;
	}

	public double[] getParammap() {
		return parammap;
	}
	
	public boolean isOnCooldown() {
		return cooldown;
	}
	
	@Override
	public String toString() {
		return "ActionData{ Trigger:["+trigger+"], self:["+self+"], target:["+target+"], item:["+item+"], damage:["+damage+"], parameter:"+Arrays.toString(parammap)+" }";
	}
}
