package de.dosmike.sponge.WarCraftMC.effects;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;

import de.dosmike.sponge.WarCraftMC.ManaPipe;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.mikestoolbox.living.CustomEffect;

public class wceManaTransfer implements CustomEffect {

	private final Player source;
	private final double duration;
	private double amount;
	private final double apms;
	
	/** 
	 * @param duration in seconds
	 * @param amount in amount over duration */
	public wceManaTransfer(Player source, double duration, double amount) {
		this.duration = duration;
		this.source = source;
		this.amount = amount;
		this.apms = amount/duration/1000.0;
	}
	
	@Override
	public String getName() {
		return "Transfer Mana";
	}

	@Override
	public double getDuration() {
		return duration;
	}

	@Override
	public boolean isRunning() {
		return Profile.isLoaded(source) && ManaPipe.getMana(source) > 0 && amount > 0;
	}
	
	@Override
	public void onTick(Living entity, int dt) {
		double transfer = dt * apms;
		if (transfer > amount) transfer = amount;
		
		double max = ManaPipe.getMana(source);
		if (transfer > max) transfer = max;
		
		ManaPipe.subMana(source, transfer);
		ManaPipe.addMana((Player)entity, transfer);
		amount -= transfer;
	}

	
}
