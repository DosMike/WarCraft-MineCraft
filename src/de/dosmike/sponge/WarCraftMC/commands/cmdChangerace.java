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
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;
import de.dosmike.sponge.WarCraftMC.Manager.KeywordedConsumer;
import de.dosmike.sponge.WarCraftMC.Manager.NextSpawnActionManager;
import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.races.Race;

public class cmdChangerace implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/changerace name - Change your race to a named race"))
			.arguments(GenericArguments.remainingJoinedStrings(Text.of("Race")))
			.permission("wc.race.change")
			.executor(new cmdChangerace())
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
			Profile profile = Profile.loadOrCreate(player);
			if (profile.getLevel()<to.get().getRequiredLevel()) {
				WarCraft.tell(player, "You need level " + to.get().getRequiredLevel() + " to change to that race (currently "+profile.getLevel()+")");
			} else {
				if (!profile.getRaceData().isPresent()) { //if the player has no race yet allow him to get one immediately
					if (profile.switchRace(to.get(), NamedCause.source(player))){
						BookMenuManager.sendRaceMenu(player);
					}
				} else { //otherwise he'll have to wait for a spawn event to prevent abuse 
					NextSpawnActionManager.force(player, new KeywordedConsumer<Player>("WarCraftChangeRace") {
						@Override
						public void accept(Player target) {
							if (profile.switchRace(to.get(), NamedCause.source(target))){
								BookMenuManager.sendRaceMenu(target);
							}
						}
					});
					WarCraft.tell(player, "Your race will be changed the next time you spawn"); //switchRace is telling this
				}
			}
		}
		return CommandResult.success();
	}
	
}
