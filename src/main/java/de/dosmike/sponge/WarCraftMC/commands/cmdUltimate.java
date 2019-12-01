package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.PermissionRegistry;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;

public class cmdUltimate implements CommandExecutor {

	public static PermissionRegistry.Permission permission = PermissionRegistry.register("ultimate", "wc.race.ultimate", Text.of("Allow access to /ability1"), PermissionDescription.ROLE_USER);
	public static LocalizedCommandSpec getCommandSpec() {
		 return LocalizedCommandSpec.builder()
			.description("commands.ultimate.description")
			.arguments(GenericArguments.none())
			.permission(permission.getId())
			.executor(new cmdUltimate())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Profile.getIfActive((Player)src).ifPresent(profile-> {
			ActionData data = ActionData.builder(Trigger.ULTIMATE)
					.setSelf((Player) src)
					.build();
			profile.getRaceData().get().fire(profile, data);
		});
		return CommandResult.success();
	}
	
}
