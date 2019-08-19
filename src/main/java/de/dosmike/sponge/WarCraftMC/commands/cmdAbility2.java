package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class cmdAbility2 implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/ability2 - Activate your 2. Skill"))
			.arguments(GenericArguments.none())
			.permission("wc.race.ability2")
			.executor(new cmdAbility2())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Profile.getIfActive((Player)src).ifPresent(profile-> {
			ActionData data = ActionData.builder(Trigger.ACTIVE2)
					.setSelf((Player) src)
					.build();
			profile.getRaceData().get().fire(profile, data);
		});
		return CommandResult.success();
	}
	
}
