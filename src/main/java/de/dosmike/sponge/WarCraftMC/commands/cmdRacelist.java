package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;
import de.dosmike.sponge.WarCraftMC.Manager.PermissionRegistry;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;

public class cmdRacelist implements CommandExecutor {

	public static PermissionRegistry.Permission permission = PermissionRegistry.register("racelist", "wc.race.list", Text.of("Allow access to /racelist and /raceinfo"), PermissionDescription.ROLE_USER);
	public static LocalizedCommandSpec getCommandSpec() {
		 return LocalizedCommandSpec.builder()
			.description("commands.racelist.description")
			.arguments(GenericArguments.optional(GenericArguments.integer(Text.of("Page"))))
			.permission(permission.getId())
			.executor(new cmdRacelist())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		int page = args.<Integer>getOne("Page").orElse(1);
		BookMenuManager.sendRaceSelect((Player)src, page);
		return CommandResult.success();
	}
	
}
