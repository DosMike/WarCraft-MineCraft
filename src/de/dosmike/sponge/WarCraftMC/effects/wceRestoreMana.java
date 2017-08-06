package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;

import de.dosmike.sponge.WarCraftMC.ManaPipe;

public class wceRestoreMana implements wcEffect {

	private final double manaPerTick;
	private final double duration;
	private final int amount;
	private double manaToGive = 0.0;
	private int manaGiven = 0;
	public wceRestoreMana(double duration, int amount) {
		this.duration = duration;
		this.amount = amount;
		this.manaPerTick = 20*amount/duration;
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
	public void onTick(Living entity) {
		if (manaGiven>=amount) return;
		manaToGive+=manaPerTick;
		int c = (int)manaToGive;
		manaGiven+=c;
		ManaPipe.addMana((Player)entity, c);
	}

	@Override
	public void onRemove(Living entity) {
		int rest = amount-manaGiven;
		if (rest>0) ManaPipe.addMana((Player)entity, rest);
	}
	
}
