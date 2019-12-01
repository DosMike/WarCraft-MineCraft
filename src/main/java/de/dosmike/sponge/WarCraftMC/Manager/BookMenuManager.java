package de.dosmike.sponge.WarCraftMC.Manager;

import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.RaceData;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.WarCraftMC.races.Skill;
import de.dosmike.sponge.WarCraftMC.wcUtils;
import de.dosmike.sponge.langswitch.LocalizedText;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
        pagetext.append(Text.of(
                WarCraft.T().localText("bookmenu.racelist.line1").orLiteral(player), Text.NEW_LINE,
                WarCraft.T().localText("bookmenu.racelist.line2")
                        .replace("$level", profile.getLevel())
                        .orLiteral(player)
        ), Text.NEW_LINE, Text.NEW_LINE);
		int i=0;
		String currentRace = profile.getRaceData().isPresent()?profile.getRaceData().get().getRace().getID():null;
		for (; i<pageOptionCount && pageo+i<races.size(); i++) {
			Race race = races.get(pageo+i);
			if (race.getID().equals(currentRace)) {
			    Text text = WarCraft.T().localText("bookmenu.racelist.entry.selected")
                        .replace("$number", page+i+1)
                        .replace("$race", race.getName())
                        .orLiteral(player);
			    pagetext.append(wcUtils.makeClickable(text, src->{
                    sendRaceMenu((Player)src);
                }).build(), Text.NEW_LINE);
			} else if (profile.getLevel() >= race.getRequiredLevel()) {
			    Text text = WarCraft.T().localText("bookmenu.racelist.entry.unlocked")
                        .replace("$number", page+i+1)
                        .replace("$race", race.getName())
                        .replace("$required", race.getRequiredLevel())
                        .orLiteral(player);
			    pagetext.append(wcUtils.makeClickable(text, src->{
			        sendRaceInfo((Player)src, race, page);
                }).build(), Text.NEW_LINE);
			} else {
			    Text text = WarCraft.T().localText("bookmenu.racelist.entry.locked")
                        .replace("$number", page+i+1)
                        .replace("$race", race.getName())
                        .replace("$level", profile.getLevel())
                        .replace("$required", race.getRequiredLevel())
                        .orLiteral(player);
			    pagetext.append(wcUtils.makeClickable(text, src->{
			        sendRaceInfo((Player)src, race, page);
                }).build(), Text.NEW_LINE);
			}
		}
		pagetext.append(Text.of(TextColors.RESET));
		if (page > 1) {
		    Text button = WarCraft.T().localText("bookmenu.racelist.previous").orLiteral(player);
		    pagetext.append(Text.of("  ", wcUtils.makeClickable(button, "/racelist "+(page-1))));
        }
		if (i == pageOptionCount && pageo+i<races.size()) {
		    Text button = WarCraft.T().localText("bookmenu.racelist.next").orLiteral(player);
		    pagetext.append(Text.of("  ", wcUtils.makeClickable(button, "/racelist "+(page+1))));
        }
		sendBook(player, Text.of("WarCraft Races"), pagetext.build());
	}
	
	/** Only shows a quick overview of this list with the option to switch to it.
	 * @param player the player for whom to edit the skills */
	public static void sendRaceInfo(Player player, Race race) { sendRaceInfo(player, race, 1); }
	public static void sendRaceInfo(Player player, Race race, int pagefrom) {
		Profile profile = Profile.loadOrCreate(player);
		
		String desc = race.getDescription(player);
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
        {
            Text text = WarCraft.T().localText("bookmenu.raceinfo.requiredlevel")
                    .replace("$level", race.getRequiredLevel())
                    .orLiteral(player);
            page1.append(Text.of(TextColors.DARK_GREEN, race.getName(player)), Text.NEW_LINE,
                    /*TextColors.RESET, */text, Text.NEW_LINE, Text.NEW_LINE);
        }
		
		for (int i = 0; i < race.getSkillCount(); i++) {
			Skill skill = race.getSkill(i);
			if (skill.getSkillLevel()==0) {
			    Text text = WarCraft.T().localText("bookmenu.raceinfo.unleveled")
                        .replace("$number", skill.getMaxSkill())
                        .orLiteral(player);
                page1.append(Text.builder()
                        .append(Text.of(TextColors.DARK_BLUE, skill.getName(player), Text.NEW_LINE,
                                text, Text.NEW_LINE))
                        .onHover(TextActions.showText(Text.of(skill.getDescription(player)))).build())
                        .build();
            } else {
			    Text text = WarCraft.T().localText("bookmenu.raceinfo.leveled")
                        .replace("$level", skill.getSkillLevel())
                        .replace("$number", skill.getMaxSkill())
                        .orLiteral(player);
                page1.append(Text.builder()
                        .append(Text.of(TextColors.DARK_GRAY, skill.getName(player), Text.NEW_LINE,
                                text, Text.NEW_LINE))
                        .onHover(TextActions.showText(Text.of(skill.getDescription(player))))
                        .build());
            }
		}

		Text bnRaceList = wcUtils.makeClickable(WarCraft.T().local("bookmenu.raceinfo.buttons.list.text").orLiteral(player),
                "/racelist "+pagefrom).build();
		Text bnChange;
		if (profile.getLevel() >= race.getRequiredLevel()) {
            bnChange = wcUtils.makeClickable(WarCraft.T().local("bookmenu.raceinfo.buttons.change.text").orLiteral(player), "/changerace "+race.getID())
                    .onHover(TextActions.showText(WarCraft.T().localText("bookmenu.raceinfo.buttons.change.hover").orLiteral(player)))
                    .color(TextColors.DARK_AQUA)
                    .build();
        } else {
            bnChange = Text.of(WarCraft.T().local("bookmenu.raceinfo.buttons.change.text").orLiteral(player));
        }
		page1.append(Text.NEW_LINE);
		Text format = ((LocalizedText)WarCraft.T().localText("bookmenu.raceinfo.buttons.format"))
                .replace("LIST", bnRaceList)
                .replace("CHANGE", bnChange)
				.setContextColor(TextColors.DARK_GRAY)
                .orLiteral(player);
		page1.append(format);
		
		descpages.add(0, page1.build());
		sendBook(player, Text.of("RaceInfo " + race.getName(player)), descpages.toArray(new Text[descpages.size()]));
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

        String desc = data.getRace().getDescription(player);
        List<Text> descpages = new LinkedList<>();
        if (!desc.trim().isEmpty()) {
//			WarCraft.l("Desc: "+desc);
            do {
                String page = desc.substring(0, Math.min(200, desc.length()));
                if (page.length() == 200) page = page.substring(0, page.lastIndexOf(' '));
                if (page.isEmpty()) break;
//				WarCraft.l("Page: " + page);
                descpages.add(Text.of(page.trim()));
                desc = desc.substring(page.length());
            } while (!desc.isEmpty());
        }

        Text.Builder page1 = Text.builder();
        page1.append(Text.of(TextColors.DARK_GREEN, data.getRace().getName(player), TextColors.RESET, Text.NEW_LINE));

        if (data.getLevel() == data.getRace().getMaxLevel()) {
            Text text = WarCraft.T().localText("bookmenu.racemenu.levelmax")
                    .replace("$level", data.getLevel())
                    .orLiteral(player);
            page1.append(Text.of(text, TextColors.RESET, Text.NEW_LINE));
        } else {
            Text text = WarCraft.T().localText("bookmenu.racemenu.levelinfo")
                    .replace("$level", data.getLevel())
                    .replace("$xp", data.getXP())
                    .replace("$required", data.getRace().getLevelXp(data.getLevel()))
                    .orLiteral(player);
            page1.append(text, Text.NEW_LINE);
        }
        {
            Text text = WarCraft.T().localText("bookmenu.racemenu.skillpoints")
                    .replace("$points", data.getSkillPoints())
                    .orLiteral(player);
            page1.append(Text.of(text, TextColors.RESET, Text.NEW_LINE, Text.NEW_LINE));
        }
		
		for (int i = 0; i < data.getRace().getSkillCount(); i++) {
			Skill skill = data.getRace().getSkill(i);
			if (skill.getMaxSkill() == data.getSkillProgress(i)) {
			    Text text = Text.of(WarCraft.T().localText("bookmenu.racemenu.skillmaxedout")
                        .replace("\\n", Text.NEW_LINE)
                        .replace("$skill", skill.getName())
                        .replace("$progress", data.getSkillProgress(i))
                        .replace("$max", skill.getMaxSkill())
                        .orLiteral(player), Text.NEW_LINE);
				page1.append(wcUtils.makeClickable(text, "/spendskill "+(i+1))
						.onHover(TextActions.showText(Text.of(TextColors.GOLD, WarCraft.T().localText("bookmenu.racemenu.hovermaxed").orLiteral(player), Text.NEW_LINE, TextColors.RESET, skill.getDescription(player)))).build());
			} else if (data.getLevel()>=skill.getSkillLevel()) {
			    Text text = Text.of(WarCraft.T().localText("bookmenu.racemenu.skillprogress")
                        .replace("\\n", Text.NEW_LINE)
                        .replace("$skill", skill.getName())
                        .replace("$progress", data.getSkillProgress(i))
                        .replace("$max", skill.getMaxSkill())
                        .orLiteral(player), Text.NEW_LINE);
                page1.append(wcUtils.makeClickable(text, "/spendskill " + (i + 1))
                        .onHover(TextActions.showText(Text.of(skill.getDescription(player)))).build());
            } else {
			    Text text = Text.of(WarCraft.T().localText("bookmenu.racemenu.skillrequires")
                        .replace("\\n", Text.NEW_LINE)
                        .replace("$skill", skill.getName())
                        .replace("$level", skill.getSkillLevel())
                        .orLiteral(player), Text.NEW_LINE);
                page1.append(Text.builder().append(text)
                        .onHover(TextActions.showText(Text.of(skill.getDescription(player)))).build());
            }
		}
		
		descpages.add(0, page1.build());
		sendBook(player, Text.of("RaceMenu " + data.getRace().getName(player)), descpages.toArray(new Text[descpages.size()]));
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
		//render skill progress bar
		page.append(Text.of(TextColors.DARK_GREEN, skill.getName(player), ":\n", TextColors.RESET));
		String s = ""; for (int i=0; i < skillProg; i++) s+='\u2588';
		String s2 = ""; for (int i=skillProg; i < skill.getMaxSkill(); i++) s2+='\u2588';
		page.append(Text.of("  ", skillProg, "/", skill.getMaxSkill(), " ", (!s.isEmpty()?Text.of(TextColors.GREEN, s):Text.EMPTY), TextColors.DARK_GRAY, s2, TextColors.RESET));

		page.append(Text.NEW_LINE, Text.NEW_LINE, WarCraft.T().localText("bookmenu.skillmenu.skillpoints")
                .replace("$points", data.getSkillPoints())
                .orLiteral(player), Text.NEW_LINE, Text.NEW_LINE
        );

		Text bnReset, bnSkillUp, bnRaceMenu;
		if (skillProg>0) {
		    bnReset = wcUtils.makeClickable(WarCraft.T().local("bookmenu.skillmenu.buttons.reset.text").orLiteral(player), src->{
		        profile.getRaceData().ifPresent(d->d.resetSkill(skillIndex-1));
		        sendSkillMenu(player, skillIndex);
            }).color(TextColors.DARK_RED).build();
        } else {
		    bnReset = Text.of(WarCraft.T().local("bookmenu.skillmenu.buttons.reset.text"));
        }
		if (skillProg<skill.getMaxSkill() && data.getSkillPoints()>0) {
		    bnSkillUp = wcUtils.makeClickable(WarCraft.T().local("bookmenu.skillmenu.buttons.skillup.text").orLiteral(player), src->{
		        profile.getRaceData().ifPresent(d->d.spendSkill(skillIndex-1));
		        sendSkillMenu(player, skillIndex);
            }).color(TextColors.DARK_GREEN).build();
        } else {
            bnSkillUp = Text.of(WarCraft.T().local("bookmenu.skillmenu.buttons.skillup.text"));
        }
		bnRaceMenu = wcUtils.makeClickable(WarCraft.T().local("bookmenu.skillmenu.buttons.racemenu.text").orLiteral(player), "/racemenu")
                .color(TextColors.DARK_AQUA).build();

		Text format1 = ((LocalizedText)WarCraft.T().localText("bookmenu.skillmenu.buttons.format1"))
                .replace("RESET", bnReset)
                .replace("SKILLUP", bnSkillUp)
				.setContextColor(TextColors.DARK_GRAY)
                .orLiteral(player);
		Text format2 = ((LocalizedText)WarCraft.T().localText("bookmenu.skillmenu.buttons.format2"))
                .replace("RACEMENU", bnRaceMenu)
				.setContextColor(TextColors.DARK_GRAY)
                .orLiteral(player);
        page.append(format1, Text.NEW_LINE,
                format2, Text.NEW_LINE, Text.NEW_LINE,
                Text.of(skill.getDescription(player)));
		
		sendBook(player, Text.of("SpendSkill " + data.getRace().getName(player)), page.build());
	}
	
	static void sendBook(Player player, Text title, Text... pages) {
		player.sendBookView(BookView.builder().addPages(pages).title(title).author(Text.of(TextColors.GOLD, "WarCraft MC")).build());
	}
}
