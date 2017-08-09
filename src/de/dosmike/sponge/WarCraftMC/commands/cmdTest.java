package de.dosmike.sponge.WarCraftMC.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.fireball.LargeFireball;
import org.spongepowered.api.text.Text;

import de.dosmike.sponge.WarCraftMC.WarCraft;

public class cmdTest implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("?"))
			.arguments(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("Speed"))))
			.permission("wc.race.dont.give")
			.executor(new cmdTest())
			.build();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }

		Player source = (Player)src;
		Double speed = (Double) args.getOne("Speed").orElse(1.0);
		Optional<LargeFireball> maybe = (source.launchProjectile(LargeFireball.class, source.getHeadRotation().mul(speed)));
		if (!maybe.isPresent()) {
			if (source instanceof Player) WarCraft.tell((Player)source, "FIREBALL! ... looks like this is not yet implemented :<");
		}
		LargeFireball fireball = maybe.get();
		fireball.setVelocity(source.getHeadRotation().mul(speed));
		
		fireball.offer(Keys.HAS_GRAVITY, false);
		
		return CommandResult.success();
	}
	
}
