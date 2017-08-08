package de.dosmike.sponge.WarCraftMC.commands;

import org.spongepowered.api.Sponge;

import de.dosmike.sponge.WarCraftMC.WarCraft;

public class CommandRegister {
	public static void RegisterCommands(WarCraft instance) {
		Sponge.getCommandManager().register(instance, cmdRacelist.getCommandSpec(), "racelist", "races");
		Sponge.getCommandManager().register(instance, cmdRaceinfo.getCommandSpec(), "raceinfo", "race");
		Sponge.getCommandManager().register(instance, cmdChangerace.getCommandSpec(), "changerace");
		Sponge.getCommandManager().register(instance, cmdRacemenu.getCommandSpec(), "racemenu");
		Sponge.getCommandManager().register(instance, cmdSpendskill.getCommandSpec(), "spendskill");
		Sponge.getCommandManager().register(instance, cmdAbility1.getCommandSpec(), "ability1", "skill1", "a1");
		Sponge.getCommandManager().register(instance, cmdAbility2.getCommandSpec(), "ability2", "skill3", "a2");
		Sponge.getCommandManager().register(instance, cmdAbility3.getCommandSpec(), "ability3", "skill2", "a3");
		Sponge.getCommandManager().register(instance, cmdUltimate.getCommandSpec(), "ultimate", "ulti", "ult");
	}
}
