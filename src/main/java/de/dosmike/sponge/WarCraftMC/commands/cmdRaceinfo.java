package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;
import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.races.Race;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class cmdRaceinfo implements CommandExecutor {

	public static LocalizedCommandSpec getCommandSpec() {
		return LocalizedCommandSpec.builder()
			.description("commands.raceinfo.description")
			.arguments(GenericArguments.remainingJoinedStrings(Text.of("Race")))
			.permission(cmdRacelist.permission.getId())
			.executor(new cmdRaceinfo())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Player player = (Player)src;
		Optional<Race> to = RaceManager.getRace((String) args.getOne("Race").orElse(""));
		//if (!to.isPresent()) RaceManager.getRaceByName((String)args.getOne("Race").orElse(""));
		if (!to.isPresent()) {
			WarCraft.tell(player, WarCraft.T().localText("commands.raceinfo.error.nosuchrace"));
		} else {
			BookMenuManager.sendRaceInfo(player, to.get());
		}
		return CommandResult.success();
	}
	
}
