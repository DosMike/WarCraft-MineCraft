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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class cmdSpendskill implements CommandExecutor {
	
	public static LocalizedCommandSpec getCommandSpec() {
		 return LocalizedCommandSpec.builder()
			.description("commands.spendskill.description")
			.arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("Skill"))))
			.permission(cmdChangerace.permission.getId())
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
				WarCraft.tell(player, WarCraft.T().localText("commands.spendskill.error.skillnumber") );
			} else {
				BookMenuManager.sendSkillMenu(player, skillIndex);
			}
		}
		return CommandResult.success();
	}
	
}
