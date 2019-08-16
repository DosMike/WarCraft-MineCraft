package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;
import de.dosmike.sponge.WarCraftMC.Manager.KeywordedConsumer;
import de.dosmike.sponge.WarCraftMC.Manager.NextSpawnActionManager;
import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.races.Race;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class cmdChangerace implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/changerace name - Change your race to a named race"))
			.arguments(
					GenericArguments.choices(Text.of("Race"),
							()->{
								List<String> suggestions = new ArrayList<>();
								RaceManager.getRaces().forEach(r->{
									suggestions.add(r.getName());
									suggestions.add(r.getID());
								});
								return suggestions;
							},
							//try to get by id, otherwise try name
							(r)-> RaceManager.getRace(r).orElseGet(()-> RaceManager.getRaceByName(r).orElse(null)),
							false)
			)
			.permission("wc.race.change")
			.executor(new cmdChangerace())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Player player = (Player)src;
		Optional<Race> to = args.getOne("Race");
		if (!to.isPresent()) {
			WarCraft.tell(player, "There is no such race");
		} else {
			Profile profile = Profile.loadOrCreate(player);
			if (profile.getLevel()<to.get().getRequiredLevel()) {
				WarCraft.tell(player, "You need level " + to.get().getRequiredLevel() + " to change to that race (currently "+profile.getLevel()+")");
			} else {
				if (!profile.getRaceData().isPresent()) { //if the player has no race yet allow him to get one immediately
//					if (profile.switchRace(to.get(), NamedCause.source(player))){
					if (profile.switchRace(to.get())){
						BookMenuManager.sendRaceMenu(player);
					}
				} else { //otherwise he'll have to wait for a spawn event to prevent abuse
					final String raceID = to.get().getID();
					NextSpawnActionManager.force(player, new KeywordedConsumer<Player>("WarCraftChangeRace") {
						@Override
						public void accept(Player target) {
//							if (profile.switchRace(to.get(), NamedCause.source(target))){
							Race race = RaceManager.getRace(raceID).orElse(null);
							if (race != null && Profile.loadOrCreate(target).switchRace(race)){
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
