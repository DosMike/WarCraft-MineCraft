package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.RaceData;
import de.dosmike.sponge.WarCraftMC.wcUtils;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.WarCraftMC.races.Skill;

public class BookMenuManager {
	static final int pageOptionCount=10; 
	
	/** A menu listing all raced by <br>
	 * 1. availability (reqired level ASC) <br>
	 * 2. name (string ASC) 
	 * @param player the player for whom to edit the skills
	 * @param page the page as natural number (1 based) */
	public static void sendRaceSelect(Player player, int page) {
		Profile profile = Profile.loadOrCreate(player);
		int pageo = (page-1)*pageOptionCount;
		List<Race> races = new LinkedList<>();
		races.addAll(RaceManager.getRaces());
		Collections.sort(races, new RaceSorter());
		
		if (pageo>races.size()) return;
		Text.Builder pagetext = Text.builder();
		pagetext.append(Text.of("Choose a Race:\nYou global Level is "+ profile.getLevel() +"\n\n"));
		int i=0;
		String currentRace = profile.getRaceData().isPresent()?profile.getRaceData().get().getRace().getID():null;
		for (; i<pageOptionCount && pageo+i<races.size(); i++) {
			Race race = races.get(pageo+i);
			if (race.getID().equals(currentRace)) {
				pagetext.append(
						wcUtils.makeClickable(Text.of(TextColors.DARK_GREEN, (pageo+i+1) + ") " + race.getName() + " [\u2714]", Text.NEW_LINE), 
								src->{
							sendRaceMenu((Player)src);
						}).build());
			} else if (profile.getLevel() >= race.getRequiredLevel()) {
				pagetext.append(
						wcUtils.makeClickable(Text.of(TextColors.BLUE, (pageo+i+1) + ") " + race.getName() + " ["+race.getRequiredLevel()+"]", Text.NEW_LINE),
								src->{
							sendRaceInfo((Player)src, race, page);
						}).build());
			} else {
				pagetext.append(
						wcUtils.makeClickable(Text.of(TextColors.DARK_GRAY, (pageo+i+1) + ") " + race.getName() + " ["+profile.getLevel()+"/"+race.getRequiredLevel()+"]", Text.NEW_LINE),
								src->{
							sendRaceInfo((Player)src, race, page);
						}).build());
			}
		}
		if (page > 1) pagetext.append(Text.of("  ", wcUtils.makeClickable("\u226A Previous", "/racemenu "+(page-1))));
		if (i == pageOptionCount-1 && pageo+i<races.size()) pagetext.append(Text.of("  ", wcUtils.makeClickable("Next \u226B", "/racemenu "+(page+1))));
		sendBook(player, Text.of("WarCraft Races"), pagetext.build());
	}
	
	/** Only shows a quick overview of this list with the option to switch to it.
	 * @param player the player for whom to edit the skills */
	public static void sendRaceInfo(Player player, Race race) { sendRaceInfo(player, race, 1); }
	public static void sendRaceInfo(Player player, Race race, int pagefrom) {
		Profile profile = Profile.loadOrCreate(player);
		
		String desc = race.getDescription();
		List<Text> descpages = new LinkedList<>();
		if (!desc.trim().isEmpty()) {
//			WarCraft.l("Desc: "+desc);
			do {
				String page = desc.substring(0, Math.min(200, desc.length()));
				if (page.length()==200) page = page.substring(0, page.lastIndexOf(' '));
				if (page.isEmpty()) break;
//				WarCraft.l("Page: " + page);
				descpages.add(Text.of(page.trim()));
				desc = desc.substring(page.length());
			} while (!desc.isEmpty());
		}
		
		Text.Builder page1 = Text.builder();
		page1.append(Text.of(TextColors.DARK_GREEN, race.getName(), TextColors.RESET, "\n Required Level: ", race.getRequiredLevel(), "\n\n"));
		
		for (int i = 0; i < race.getSkillCount(); i++) {
			Skill skill = race.getSkill(i);
			if (skill.getSkillLevel()==0)
				page1.append(Text.builder()
						.append(Text.of(TextColors.DARK_BLUE, skill.getName(), TextColors.BLUE, "\n  ", skill.getMaxSkill(), " Ranks\n"))
						.onHover(TextActions.showText(Text.of(skill.getDescription()))).build())
						.build();
			else
				page1.append(Text.builder()
						.append(Text.of(TextColors.DARK_GRAY, skill.getName(), "\n  Lvl ", skill.getSkillLevel(), ", ", skill.getMaxSkill(), " Ranks\n"))
						.onHover(TextActions.showText(Text.of(skill.getDescription())))
						.build());
		}
		
		page1.append(Text.of(TextColors.RESET, "[", wcUtils.makeClickable("Race List", "/racelist "+pagefrom).build(), TextColors.RESET, "]  [", 
				profile.getLevel() >= race.getRequiredLevel()
				? wcUtils.makeClickable("Change", "/changerace "+race.getID()).onHover(TextActions.showText(Text.of("The change might not take effect until you respawn"))).color(TextColors.DARK_AQUA).build()
				: Text.of("Change"),
				TextColors.RESET, "]"));
		
		descpages.add(0, page1.build());
		sendBook(player, Text.of("RaceInfo " + race.getName()), descpages.toArray(new Text[descpages.size()]));
	}
	
	/** A menu to improve skills of a player if available
	 * <pre>
	 * RACENAME
	 *   Lvl X ?/??XP
	 * You can spend ?SP
	 *
	 * SKILLNAME
	 *   [8/8] Maxed out
	 * SKILLNAME
	 *   [1/8]
	 * SKILLNAME
	 *   requires Lvl 8
	 * </pre>
	 * @param player the player for whom to edit the skills */
	public static void sendRaceMenu(Player player) {
		Profile profile = Profile.loadOrCreate(player);
		if (!profile.getRaceData().isPresent()) return;
		RaceData data = profile.getRaceData().get();
		
		String desc = data.getRace().getDescription();
		List<Text> descpages = new LinkedList<>();
		if (!desc.trim().isEmpty()) {
//			WarCraft.l("Desc: "+desc);
			do {
				String page = desc.substring(0, Math.min(200, desc.length()));
				if (page.length()==200) page = page.substring(0, page.lastIndexOf(' '));
				if (page.isEmpty()) break;
//				WarCraft.l("Page: " + page);
				descpages.add(Text.of(page.trim()));
				desc = desc.substring(page.length());
			} while (!desc.isEmpty());
		}
		
		Text.Builder page1 = Text.builder();
		page1.append(Text.of(TextColors.DARK_GREEN, data.getRace().getName(), TextColors.RESET, "\n  Lvl ", data.getLevel()));
		if (data.getLevel()==data.getRace().getMaxLevel())
			page1.append(Text.of(TextColors.DARK_RED, "  Max Lvl", TextColors.RESET, "\n\n"));
		else
			page1.append(Text.of(TextColors.DARK_RED, " ", data.getXP(), "/", data.getRace().getLevelXp(data.getLevel()), TextColors.RESET, "XP\n"));
		page1.append(Text.of("You can spend ", TextColors.DARK_RED, data.getSkillPoints(), TextColors.RESET, "SP\n\n"));
		
		for (int i = 0; i < data.getRace().getSkillCount(); i++) {
			Skill skill = data.getRace().getSkill(i);
			if (skill.getMaxSkill() == data.getSkillProgress(i)) {
				page1.append(
						wcUtils.makeClickable(Text.of(TextColors.DARK_PURPLE, TextStyles.ITALIC, skill.getName(), TextStyles.RESET, TextColors.LIGHT_PURPLE, "\n  [", data.getSkillProgress(i), "/", skill.getMaxSkill(), "]\n"), "/spendskill "+(i+1))
						.onHover(TextActions.showText(Text.of(TextColors.GOLD, "Maxed out\n", TextColors.RESET, skill.getDescription()))).build());
			} else if (data.getLevel()>=skill.getSkillLevel())
				page1.append(
						wcUtils.makeClickable(Text.of(TextColors.DARK_BLUE, skill.getName(), TextColors.BLUE, "\n  [", data.getSkillProgress(i), "/", skill.getMaxSkill(), "]\n"), "/spendskill "+(i+1))
						.onHover(TextActions.showText(Text.of(skill.getDescription()))).build());
			else
				page1.append(Text.builder()
						.append(Text.of(TextColors.DARK_GRAY, skill.getName(), "\n  reqires Lvl ", skill.getSkillLevel(), "\n"))
						.onHover(TextActions.showText(Text.of(skill.getDescription()))).build());
		}
		
		descpages.add(0, page1.build());
		sendBook(player, Text.of("RaceMenu " + data.getRace().getName()), descpages.toArray(new Text[descpages.size()]));
	}
	
	/** A menu to rest or skill up a selected (skillIndex) skill
	 * <pre>
	 * SKILLNAME:
	 *   1/8 ████████
	 *
	 * You can spend ?SP
	 *
	 * [Reset]       [Skill Up]
	 *       [Race Menu]
	 * </pre>
	 * @param player the player for whom to edit the skill
	 * @param skillIndex skill index as natural number (1 based) */
	public static void sendSkillMenu(Player player, int skillIndex) {
		Profile profile = Profile.loadOrCreate(player);
		if (!profile.getRaceData().isPresent()) return;
		RaceData data = profile.getRaceData().get();
		if (skillIndex < 1 || skillIndex > data.getRace().getSkillCount()) return;
		Skill skill = data.getRace().getSkill(skillIndex-1);
		if (data.getLevel() < skill.getSkillLevel()) return;
		int skillProg = data.getSkillProgress(skillIndex-1);
		
		Text.Builder page = Text.builder();
		page.append(Text.of(TextColors.DARK_GREEN, skill.getName(), ":\n", TextColors.RESET));
		String s = ""; for (int i=0; i < skillProg; i++) s+='\u2588';
		String s2 = ""; for (int i=skillProg; i < skill.getMaxSkill(); i++) s2+='\u2588';
		page.append(Text.of("  ", skillProg, "/", skill.getMaxSkill(), " ", (!s.isEmpty()?Text.of(TextColors.GREEN, s):Text.EMPTY), TextColors.DARK_GRAY, s2, TextColors.RESET));
		
		page.append(Text.of("\n\nYou can spend ", TextColors.DARK_RED, data.getSkillPoints(), TextColors.RESET,"SP\n\n"));
		
		page.append(Text.of("[",
		(skillProg>0
		?	wcUtils.makeClickable("Reset", clicking -> {
			Optional<RaceData> maybe = profile.getRaceData();
			if (maybe.isPresent()) maybe.get().resetSkill(skillIndex-1);
			sendSkillMenu(player, skillIndex);
		}).color(TextColors.DARK_RED).build()
		:   Text.of("Reset")),
		TextColors.RESET, "]       [",
		(skillProg<skill.getMaxSkill() && data.getSkillPoints()>0
		?	wcUtils.makeClickable("Skill Up", clicking -> {
			Optional<RaceData> maybe = profile.getRaceData();
			if (maybe.isPresent()) maybe.get().spendSkill(skillIndex-1);
			sendSkillMenu(player, skillIndex);
		}).color(TextColors.DARK_GREEN).build()
		:   Text.of("Skill Up")),
		TextColors.RESET, "]\n      [", wcUtils.makeClickable("Race Menu", "/racemenu").color(TextColors.DARK_AQUA).build() ,
		TextColors.RESET, "]\n\n", skill.getDescription()));
		
		sendBook(player, Text.of("SpendSkill " + data.getRace().getName()), page.build());
	}
	
	static void sendBook(Player player, Text title, Text... pages) {
//		pages[0] = Text.of("       [WarCraft]\n\n", pages[0]);
		player.sendBookView(BookView.builder().addPages(pages).title(title).author(Text.of(TextColors.GOLD, "WarCraft MC")).build());
	}
}
