package de.dosmike.sponge.WarCraftMC.effects;

import de.dosmike.sponge.WarCraftMC.ManaPipe;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;

public class wceRestoreMana implements WarCraftCustomEffect {

	private final double manaPerSecond;
	private final double duration;
	private final double amount;
	private double manaGiven = 0;
	public wceRestoreMana(double duration, double amount) {
		this.duration = duration;
		this.amount = amount;
		this.manaPerSecond = (amount/duration);
	}
	
	@Override
	public String getName() {
		return "Restore Mana";
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
	}

	@Override
	public void onTick(Living entity, int dt) {
		if (manaGiven>=amount) return;
		double manaPerTick=manaPerSecond*(double)dt/1000.0;
		manaGiven+=manaPerTick;
		ManaPipe.addMana((Player)entity, manaPerTick);
	}

	@Override
	public void onRemove(Living entity) {
		double rest = (double)amount-manaGiven;
		if (rest>0) ManaPipe.addMana((Player)entity, rest);
	}
	
}
