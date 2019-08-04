package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.RaceData;
import de.dosmike.sponge.WarCraftMC.XPpipe;
import de.dosmike.sponge.WarCraftMC.races.Action;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import de.dosmike.sponge.WarCraftMC.races.Race;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class cmdWCAdmin implements CommandExecutor {

	private static final HashMap<String, Character> OPTION_TYPE = new HashMap<>();
	static {
		OPTION_TYPE.put("XP", 'x');
		OPTION_TYPE.put("Level", 'l');
	}

	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("Usage: /wcadmin <player> give [race] xp|level <amount>",Text.NEW_LINE,
								"Usage: /wcadmin <player> reset <race>|player",Text.NEW_LINE,
								"Usage: /wcadmin <player> force <race>"))
			.arguments(
					GenericArguments.seq(
							GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
							GenericArguments.firstParsing(
									GenericArguments.seq(
											GenericArguments.literal(Text.of("action_give"), "give"),
											GenericArguments.optional(GenericArguments.choices(Text.of("race"),
													()->RaceManager.getRaces().stream().map(Race::getID).collect(Collectors.toList()),
													(r)->RaceManager.getRace(r).orElse(null),
													false
											)),
											GenericArguments.choices(Text.of("type"), OPTION_TYPE, false, false),
											GenericArguments.integer(Text.of("amount"))
									),
									GenericArguments.seq(
											GenericArguments.literal(Text.of("action_reset"), "reset"),
											GenericArguments.choices(Text.of("race"),
													()->{
														List<String> races = RaceManager.getRaces().stream().map(Race::getID).collect(Collectors.toList());
														races.add(0, "PLAYER");
														return races;
													},
													(r)->r,
													false
											)
									),
									GenericArguments.seq(
											GenericArguments.literal(Text.of("action_force"), "force"),
											GenericArguments.choices(Text.of("race"),
													()->RaceManager.getRaces().stream().map(Race::getID).collect(Collectors.toList()),
													(r)->RaceManager.getRace(r).orElse(null),
													false
											)
									)

							)
					)
			)
			.permission("wc.admin")
			.executor(new cmdWCAdmin())
			.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Player target = args.<Player>getOne("player").get();
		Profile profile = Profile.loadOrCreate(target);
		Map<Race, RaceData> playerDump = Profile.dumpRaceData(target); //contains all loaded races

		if (args.<Boolean>getOne("action_give").orElse(false)) {
			if (XPpipe.getMode().equals(XPpipe.Mode.REFLECT)) {
				throw new CommandException(Text.of(TextColors.RED, "[WC] WarCraft is currently using the Minecraft XP-System, please use /xp"));
			}

			Optional<RaceData> race = args.<Race>getOne("race").map(playerDump::get);
			if (!race.isPresent()) race = profile.getRaceData(); //no 1 fallback for optional argument
			if (!race.isPresent()) //fallback 2: player never chose a race and no argument provided
				throw new CommandException(Text.of("Player "+target.getName()+" has no current race, please specify manually"));
			char type = args.<Character>getOne("type").orElse('\0');
			int amount = args.<Integer>getOne("amount").orElse(0);
			if (amount < 1)
				throw new CommandException(Text.of("Amount must be positive"));
			if (type == 'x') { //give XP
				if (race.get().equals(profile.getRaceData().orElse(null))) {
					//if the player currently has the modified race and update xp bar (replace)
					XPpipe.processWarCraftXP(profile, amount);
				} else {
					//otherwise push xp into that race data
					RaceData data = race.get();
					data.giveXP(amount);
					Profile.forceRaceData(target, data);
				}

				if (src != target)
					src.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "You granted "+target.getName()+" "+amount+" XP as "+race.get().getRace().getName()));
				if (!(src instanceof ConsoleSource))
					Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, src.getName()+" granted "+target.getName()+" "+amount+" XP as "+race.get().getRace().getName()));
				target.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "You were granted "+amount+" XP as "+race.get().getRace().getName()));
			} else if (type == 'l') { //give level
				long XPaccu = 0;
				RaceData data = race.get();
				int lvlcnt = data.getLevel();
				//add the xp required to level up for this and the next amount-1 level
				for (int i = 0; i < amount && lvlcnt < data.getRace().getMaxLevel(); i++, lvlcnt++) {
					XPaccu += data.getRace().getLevelXp(lvlcnt);
				}
				//batch add the xp
				if (race.get().equals(profile.getRaceData().orElse(null))) {
					//if the player currently has the modified race and update xp bar (replace)
					XPpipe.processWarCraftXP(profile, XPaccu);
				} else {
					//otherwise push xp into that race data
					data.giveXP(XPaccu);
					Profile.forceRaceData(target, data);
				}

				if (src != target)
					src.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "You granted "+target.getName()+" "+amount+" Level(s) as "+race.get().getRace().getName()));
				if (!(src instanceof ConsoleSource))
					Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, src.getName()+" granted "+target.getName()+" "+amount+" Level(s) as "+race.get().getRace().getName()));
				target.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "You were granted "+amount+" Level(s) as "+race.get().getRace().getName()));
			} else throw new CommandException(Text.of("No such option"));
		} else if (args.<Boolean>getOne("action_reset").orElse(false)) {
			String s = args.<String>getOne("race").orElseThrow(()->new CommandException(Text.of(TextColors.RED, "[WC] Invalid value for <Race>")));
			if (s.equalsIgnoreCase("player")) {
				if (!profile.delete())
					throw new CommandException(Text.of(TextColors.RED, "[WC] Unable to delete profile "+target.getUniqueId()));


				if (src != target)
					src.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "You complete reset "+target.getName()));
				if (!(src instanceof ConsoleSource))
					Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, src.getName()+" completely reset "+target.getName()));
				target.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "Your account was just reset"));
			} else {
				RaceData race = playerDump.get(
						RaceManager.getRace(s).orElseThrow(()->new CommandException(Text.of(TextColors.RED, "[WC] Could not find race (IDs are case sensitive)")))
				);
				race.reset();
				Profile.forceRaceData(target, race);

				if (src != target)
					src.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "You reset the progress of "+target.getName()+" as "+race.getRace().getName()));
				if (!(src instanceof ConsoleSource))
					Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, src.getName()+" reset the progress of "+target.getName()+" as "+race.getRace().getName()));
				target.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "Your progress as "+race.getRace().getName()+" was reset"));
			}
		} else if (args.<Boolean>getOne("action_force").orElse(false)) {
			Race race = args.<Race>getOne("race").get();
			if (!profile.switchRace(race)) {
				throw new CommandException(Text.of("Changing race was prevented by another plugin"));
			}

			if (src != target)
				src.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "You forced "+target.getName()+" into race "+race.getName()));
			if (!(src instanceof ConsoleSource))
				Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, src.getName()+" forced "+target.getName()+" into race "+race.getName()));
			target.sendMessage(Text.of(TextColors.RED, "[WC] ", TextColors.GOLD, "Your were forced into race "+race.getName()));
		} else throw new CommandException(Text.of(TextColors.RED, "[WC] Illegal state :o"));

		return CommandResult.success();
	}

}
