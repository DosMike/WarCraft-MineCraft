package de.dosmike.sponge.WarCraftMC.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;
import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.races.Race;

public class cmdRaceinfo implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/raceinfo name - Display some information about a race"))
			.arguments(GenericArguments.remainingJoinedStrings(Text.of("Race")))
			.permission("wc.race.list")
			.executor(new cmdRaceinfo())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Player player = (Player)src;
		Optional<Race> to = RaceManager.getRace((String) args.getOne("Race").orElse(""));
		if (!to.isPresent()) RaceManager.getRaceByName((String)args.getOne("Race").orElse(""));
		if (!to.isPresent()) {
			WarCraft.tell(player, "There is no such race");
		} else {
			BookMenuManager.sendRaceInfo(player, to.get());
		}
		return CommandResult.success();
	}
	
}
