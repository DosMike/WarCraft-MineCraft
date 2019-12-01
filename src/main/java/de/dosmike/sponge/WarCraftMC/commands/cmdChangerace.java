package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.*;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.langswitch.LocalizedText;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class cmdChangerace implements CommandExecutor {

	public static PermissionRegistry.Permission permission = PermissionRegistry.register("changerace", "wc.race.change", Text.of("Allow access to /changerace and /spendskill"), PermissionDescription.ROLE_USER);
	public static LocalizedCommandSpec getCommandSpec() {
		return LocalizedCommandSpec.builder()
			.description("commands.changerace.description")
			.arguments( //can an argument access the caller to provide localized suggestions?
					GenericArguments.choices(Text.of("Race"),
							()->{
								List<String> suggestions = new ArrayList<>();
								RaceManager.getRaces().forEach(r->{
									//suggestions.add(r.getName());
									suggestions.add(r.getID());
								});
								return suggestions;
							},
//							//try to get by id, otherwise try name
//							(r)-> RaceManager.getRace(r).orElseGet(()-> RaceManager.getRaceByName(r).orElse(null)),
							(r)-> RaceManager.getRace(r).orElse(null),
							false)
			)
			.permission(permission.getId())
			.executor(new cmdChangerace())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Player player = (Player)src;
		Optional<Race> to = args.getOne("Race");
		if (!to.isPresent()) {
			WarCraft.tell(player, WarCraft.T().localText("commands.changerace.error.nosuchrace"));
		} else {
			PermissionRegistry.Permission permission = PermissionRegistry.getPermission(to.get());
			if (!permission.hasPermission(src)) {
				throw new CommandException(((LocalizedText)WarCraft.T().localText("commands.changerace.error.permission"))
						.replace("$race", to.get().getName())
						.replace("$permission", permission.getId())
						.setContextColor(TextColors.RED)
						.orLiteral(src));
			}
			Profile profile = Profile.loadOrCreate(player);
			if (profile.getLevel()<to.get().getRequiredLevel()) {
				WarCraft.tell(player, WarCraft.T().localText("commands.changerace.error.level")
						.replace("$required", to.get().getRequiredLevel())
						.replace("$current", profile.getLevel()) );
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
					WarCraft.tell(player, WarCraft.T().localText("commands.changerace.success")); //switchRace is telling this
				}
			}
		}
		return CommandResult.success();
	}
	
}
