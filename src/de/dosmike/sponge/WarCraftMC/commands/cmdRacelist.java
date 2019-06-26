package de.dosmike.sponge.WarCraftMC.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;

public class cmdRacelist implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/race [page] - Display a race list"))
			.arguments(GenericArguments.optional(GenericArguments.integer(Text.of("Page"))))
			.permission("wc.race.list")
			.executor(new cmdRacelist())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		int page = (int) args.getOne("Page").orElse(1);
		BookMenuManager.sendRaceSelect((Player)src, page);
		return CommandResult.success();
	}
	
}
