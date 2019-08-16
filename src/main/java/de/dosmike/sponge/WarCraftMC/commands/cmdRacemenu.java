package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;
import de.dosmike.sponge.WarCraftMC.Profile;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class cmdRacemenu implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/racemenu - Take a look at your current race stats"))
			.permission("wc.race.menu")
			.executor(new cmdRacemenu())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Player player = (Player)src;
		Profile profile = Profile.loadOrCreate(player);
		if (profile.getRaceData().isPresent()) {
			BookMenuManager.sendRaceMenu(player);
		}
		return CommandResult.success();
	}
	
}
