package de.dosmike.sponge.WarCraftMC.commands;

import de.dosmike.sponge.WarCraftMC.Manager.BookMenuManager;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.RaceData;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class cmdSpendskill implements CommandExecutor {
	
	public static CommandSpec getCommandSpec() {
		 return CommandSpec.builder()
			.description(Text.of("/spendskill <Skill> - Open the menu to spend points on a Skill by number"))
			.arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("Skill"))))
			.permission("wc.race.change")
			.executor(new cmdSpendskill())
			.build();
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) { src.sendMessage(Text.of("Console can't do this")); return CommandResult.success(); }
		Player player = (Player)src;
		Profile profile = Profile.loadOrCreate(player);
		if (profile.getRaceData().isPresent()) {
			int skillIndex = (int) args.getOne("Skill").orElse(1);
			RaceData data = profile.getRaceData().get();
			if (skillIndex < 1 || skillIndex > data.getRace().getSkillCount()) {
				WarCraft.tell(player, "Invalid Skill number");
			} else {
				BookMenuManager.sendSkillMenu(player, skillIndex);
			}
		}
		return CommandResult.success();
	}
	
}
