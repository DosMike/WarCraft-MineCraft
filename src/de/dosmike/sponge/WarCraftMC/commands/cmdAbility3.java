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

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.WarCraftMC.races.ActionData;

public class cmdAbility3 implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/ability3 - Activate your 3. Skill"))
			.arguments(GenericArguments.none())
			.permission("wc.race.ability3")
			.executor(new cmdAbility3())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Profile profile = Profile.loadOrCreate((Player)src);
		ActionData data = ActionData.builder(Trigger.ACTIVE3)
				.setSelf((Player)src)
				.build();
		if (profile.isActive()) profile.getRaceData().get().fire(profile, data);
		return CommandResult.success();
	}
	
}
