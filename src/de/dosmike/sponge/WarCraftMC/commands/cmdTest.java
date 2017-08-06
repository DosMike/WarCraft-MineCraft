package de.dosmike.sponge.WarCraftMC.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import de.dosmike.sponge.WarCraftMC.WarCraft;

public class cmdTest implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/test - ?"))
			.arguments(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("Val"))))
			.permission("wc.race.ability1")
			.executor(new cmdTest())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }

		double amount=(double) args.getOne("Val").orElse(20.0);
		WarCraft.l("Giving "+src+" "+amount+"hp");
		Player p = (Player) src;
		p.offer(Keys.MAX_HEALTH, amount>20.0?amount:20.0);
		p.offer(Keys.HEALTH, amount);
		
		return CommandResult.success();
	}
	
}
