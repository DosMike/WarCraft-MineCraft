package de.dosmike.sponge.WarCraftMC.skills;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.effects.wceTraceBeacon;
import de.dosmike.sponge.WarCraftMC.effects.wceTraceLine;
import de.dosmike.sponge.WarCraftMC.wcSkill;
import de.dosmike.sponge.WarCraftMC.wcUtils;
import de.dosmike.sponge.mikestoolbox.living.BoxLiving;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SkillEffects {

	private static final Map<String, Color> colorNames = new HashMap<>();
	static {
		colorNames.put("indianred",				Color.ofRgb(0xCD5C5C));
		colorNames.put("lightcoral",			Color.ofRgb(0xF08080));
		colorNames.put("salmon",				Color.ofRgb(0xFA8072));
		colorNames.put("darksalmon",			Color.ofRgb(0xE9967A));
		colorNames.put("lightsalmon",			Color.ofRgb(0xFFA07A));
		colorNames.put("crimson",				Color.ofRgb(0xDC143C));
		colorNames.put("red",					Color.ofRgb(0xFF0000));
		colorNames.put("firebrick",				Color.ofRgb(0xB22222));
		colorNames.put("darkred",				Color.ofRgb(0x8B0000));
		colorNames.put("pink",					Color.ofRgb(0xFFC0CB));
		colorNames.put("lightpink",				Color.ofRgb(0xFFB6C1));
		colorNames.put("hotpink",				Color.ofRgb(0xFF69B4));
		colorNames.put("deeppink",				Color.ofRgb(0xFF1493));
		colorNames.put("mediumvioletred",		Color.ofRgb(0xC71585));
		colorNames.put("palevioletred",			Color.ofRgb(0xDB7093));
		colorNames.put("coral",					Color.ofRgb(0xFF7F50));
		colorNames.put("tomato",				Color.ofRgb(0xFF6347));
		colorNames.put("orangered",				Color.ofRgb(0xFF4500));
		colorNames.put("darkorange",			Color.ofRgb(0xFF8C00));
		colorNames.put("orange",				Color.ofRgb(0xFFA500));
		colorNames.put("gold",					Color.ofRgb(0xFFD700));
		colorNames.put("yellow",				Color.ofRgb(0xFFFF00));
		colorNames.put("lightyellow",			Color.ofRgb(0xFFFFE0));
		colorNames.put("lemonchiffon",			Color.ofRgb(0xFFFACD));
		colorNames.put("lightgoldenrodyellow",	Color.ofRgb(0xFAFAD2));
		colorNames.put("papayawhip",			Color.ofRgb(0xFFEFD5));
		colorNames.put("moccasin",				Color.ofRgb(0xFFE4B5));
		colorNames.put("peachpuff",				Color.ofRgb(0xFFDAB9));
		colorNames.put("palegoldenrod",			Color.ofRgb(0xEEE8AA));
		colorNames.put("khaki",					Color.ofRgb(0xF0E68C));
		colorNames.put("darkkhaki",				Color.ofRgb(0xBDB76B));
		colorNames.put("lavender",				Color.ofRgb(0xE6E6FA));
		colorNames.put("thistle",				Color.ofRgb(0xD8BFD8));
		colorNames.put("plum",					Color.ofRgb(0xDDA0DD));
		colorNames.put("violet",				Color.ofRgb(0xEE82EE));
		colorNames.put("orchid",				Color.ofRgb(0xDA70D6));
		colorNames.put("fuchsia",				Color.ofRgb(0xFF00FF));
		colorNames.put("magenta",				Color.ofRgb(0xFF00FF));
		colorNames.put("mediumorchid",			Color.ofRgb(0xBA55D3));
		colorNames.put("mediumpurple",			Color.ofRgb(0x9370DB));
		colorNames.put("amethyst",				Color.ofRgb(0x9966CC));
		colorNames.put("blueviolet",			Color.ofRgb(0x8A2BE2));
		colorNames.put("darkviolet",			Color.ofRgb(0x9400D3));
		colorNames.put("darkorchid",			Color.ofRgb(0x9932CC));
		colorNames.put("darkmagenta",			Color.ofRgb(0x8B008B));
		colorNames.put("purple",				Color.ofRgb(0x800080));
		colorNames.put("indigo",				Color.ofRgb(0x4B0082));
		colorNames.put("slateblue",				Color.ofRgb(0x6A5ACD));
		colorNames.put("darkslateblue",			Color.ofRgb(0x483D8B));
		colorNames.put("mediumslateblue",		Color.ofRgb(0x7B68EE));
		colorNames.put("greenyellow",			Color.ofRgb(0xADFF2F));
		colorNames.put("chartreuse",			Color.ofRgb(0x7FFF00));
		colorNames.put("lawngreen",				Color.ofRgb(0x7CFC00));
		colorNames.put("lime",					Color.ofRgb(0x00FF00));
		colorNames.put("limegreen",				Color.ofRgb(0x32CD32));
		colorNames.put("palegreen",				Color.ofRgb(0x98FB98));
		colorNames.put("lightgreen",			Color.ofRgb(0x90EE90));
		colorNames.put("mediumspringgreen",		Color.ofRgb(0x00FA9A));
		colorNames.put("springgreen",			Color.ofRgb(0x00FF7F));
		colorNames.put("mediumseagreen",		Color.ofRgb(0x3CB371));
		colorNames.put("seagreen",				Color.ofRgb(0x2E8B57));
		colorNames.put("forestgreen",			Color.ofRgb(0x228B22));
		colorNames.put("green",					Color.ofRgb(0x008000));
		colorNames.put("darkgreen",				Color.ofRgb(0x006400));
		colorNames.put("yellowgreen",			Color.ofRgb(0x9ACD32));
		colorNames.put("olivedrab",				Color.ofRgb(0x6B8E23));
		colorNames.put("olive",					Color.ofRgb(0x808000));
		colorNames.put("darkolivegreen",		Color.ofRgb(0x556B2F));
		colorNames.put("mediumaquamarine",		Color.ofRgb(0x66CDAA));
		colorNames.put("darkseagreen",			Color.ofRgb(0x8FBC8F));
		colorNames.put("lightseagreen",			Color.ofRgb(0x20B2AA));
		colorNames.put("darkcyan",				Color.ofRgb(0x008B8B));
		colorNames.put("teal",					Color.ofRgb(0x008080));
		colorNames.put("aqua",					Color.ofRgb(0x00FFFF));
		colorNames.put("cyan",					Color.ofRgb(0x00FFFF));
		colorNames.put("lightcyan",				Color.ofRgb(0xE0FFFF));
		colorNames.put("paleturquoise",			Color.ofRgb(0xAFEEEE));
		colorNames.put("aquamarine",			Color.ofRgb(0x7FFFD4));
		colorNames.put("turquoise",				Color.ofRgb(0x40E0D0));
		colorNames.put("mediumturquoise",		Color.ofRgb(0x48D1CC));
		colorNames.put("darkturquoise",			Color.ofRgb(0x00CED1));
		colorNames.put("cadetblue",				Color.ofRgb(0x5F9EA0));
		colorNames.put("steelblue",				Color.ofRgb(0x4682B4));
		colorNames.put("lightsteelblue",		Color.ofRgb(0xB0C4DE));
		colorNames.put("powderblue",			Color.ofRgb(0xB0E0E6));
		colorNames.put("lightblue",				Color.ofRgb(0xADD8E6));
		colorNames.put("skyblue",				Color.ofRgb(0x87CEEB));
		colorNames.put("lightskyblue",			Color.ofRgb(0x87CEFA));
		colorNames.put("deepskyblue",			Color.ofRgb(0x00BFFF));
		colorNames.put("dodgerblue",			Color.ofRgb(0x1E90FF));
		colorNames.put("cornflowerblue",		Color.ofRgb(0x6495ED));
		colorNames.put("royalblue",				Color.ofRgb(0x4169E1));
		colorNames.put("blue",					Color.ofRgb(0x0000FF));
		colorNames.put("mediumblue",			Color.ofRgb(0x0000CD));
		colorNames.put("darkblue",				Color.ofRgb(0x00008B));
		colorNames.put("navy",					Color.ofRgb(0x000080));
		colorNames.put("midnightblue",			Color.ofRgb(0x191970));
		colorNames.put("cornsilk",				Color.ofRgb(0xFFF8DC));
		colorNames.put("blanchedalmond",		Color.ofRgb(0xFFEBCD));
		colorNames.put("bisque",				Color.ofRgb(0xFFE4C4));
		colorNames.put("navajowhite",			Color.ofRgb(0xFFDEAD));
		colorNames.put("wheat",					Color.ofRgb(0xF5DEB3));
		colorNames.put("burlywood",				Color.ofRgb(0xDEB887));
		colorNames.put("tan",					Color.ofRgb(0xD2B48C));
		colorNames.put("rosybrown",				Color.ofRgb(0xBC8F8F));
		colorNames.put("sandybrown",			Color.ofRgb(0xF4A460));
		colorNames.put("goldenrod",				Color.ofRgb(0xDAA520));
		colorNames.put("darkgoldenrod",			Color.ofRgb(0xB8860B));
		colorNames.put("peru",					Color.ofRgb(0xCD853F));
		colorNames.put("chocolate",				Color.ofRgb(0xD2691E));
		colorNames.put("saddlebrown",			Color.ofRgb(0x8B4513));
		colorNames.put("sienna",				Color.ofRgb(0xA0522D));
		colorNames.put("brown",					Color.ofRgb(0xA52A2A));
		colorNames.put("maroon",				Color.ofRgb(0x800000));
		colorNames.put("white",					Color.ofRgb(0xFFFFFF));
		colorNames.put("snow",					Color.ofRgb(0xFFFAFA));
		colorNames.put("honeydew",				Color.ofRgb(0xF0FFF0));
		colorNames.put("mintcream",				Color.ofRgb(0xF5FFFA));
		colorNames.put("azure",					Color.ofRgb(0xF0FFFF));
		colorNames.put("aliceblue",				Color.ofRgb(0xF0F8FF));
		colorNames.put("ghostwhite",			Color.ofRgb(0xF8F8FF));
		colorNames.put("whitesmoke",			Color.ofRgb(0xF5F5F5));
		colorNames.put("seashell",				Color.ofRgb(0xFFF5EE));
		colorNames.put("beige",					Color.ofRgb(0xF5F5DC));
		colorNames.put("oldlace",				Color.ofRgb(0xFDF5E6));
		colorNames.put("floralwhite",			Color.ofRgb(0xFFFAF0));
		colorNames.put("ivory",					Color.ofRgb(0xFFFFF0));
		colorNames.put("antiquewhite",			Color.ofRgb(0xFAEBD7));
		colorNames.put("linen",					Color.ofRgb(0xFAF0E6));
		colorNames.put("lavenderblush",			Color.ofRgb(0xFFF0F5));
		colorNames.put("mistyrose",				Color.ofRgb(0xFFE4E1));
		colorNames.put("gainsboro",				Color.ofRgb(0xDCDCDC));
		colorNames.put("lightgrey",				Color.ofRgb(0xD3D3D3));
		colorNames.put("silver",				Color.ofRgb(0xC0C0C0));
		colorNames.put("darkgray",				Color.ofRgb(0xA9A9A9));
		colorNames.put("gray",					Color.ofRgb(0x808080));
		colorNames.put("dimgray",				Color.ofRgb(0x696969));
		colorNames.put("lightslategray",		Color.ofRgb(0x778899));
		colorNames.put("slategray",				Color.ofRgb(0x708090));
		colorNames.put("darkslategray",			Color.ofRgb(0x2F4F4F));
		colorNames.put("black",					Color.ofRgb(0x000000));
	}

	@wcSkill("playsound")
	public static SkillResult effectPlaysound(Living source, String sound) {
		Optional<SoundType> snd = wcUtils.getTypeByName(sound, SoundTypes.class);
		if (snd.isPresent())
			source.getWorld().playSound(snd.get(), SoundCategories.PLAYER, source.getLocation().getPosition(), 1.0);
		else
			source.getWorld().playSound(SoundType.builder().build(sound), source.getLocation().getPosition(), 1.0);
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("tell")
	public static SkillResult effectTell(Living target, String message) {
		if (!(target instanceof Player)) return new SkillResult().push(ResultProperty.SUCCESS, false);
		WarCraft.tell((Player)target, message);
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}
	
	@wcSkill("broadcast")
	public static SkillResult effectTell(String message) {
		WarCraft.tell(message);
		return new SkillResult().push(ResultProperty.SUCCESS, true);
	}

	@wcSkill("traceline")
	public static SkillResult effectTrace(Living source, Living target, String color, Double duration) {
		Color cval;
		if (color.startsWith("#")) cval = Color.ofRgb(Integer.parseInt(color.substring(1),16));
		else cval = colorNames.get(color.toLowerCase());

		if (cval==null) {
			WarCraft.w("Could not fire effect tract: Invalid color \""+color+"\"");
		} else if (duration >= 0) {
			BoxLiving.addCustomEffect(source, new wceTraceLine(
					source.getLocation().getPosition().add(0,1,0),
					target.getLocation().getPosition().add(0,1,0),
					cval, Math.max(0,duration)), false);
		}
		return new SkillResult().push(ResultProperty.SUCCESS, true); //always succeed to not interrupt chain
	}

	@wcSkill("tracebeacon")
	public static SkillResult effectTrace(Living target, Double radiusFrom, Double radiusTo, String color, Double duration) {
		Color cval;
		if (color.startsWith("#")) cval = Color.ofRgb(Integer.parseInt(color.substring(1),16));
		else cval = colorNames.get(color.toLowerCase());

		if (cval==null) {
			WarCraft.w("Could not fire effect tract: Invalid color \""+color+"\"");
		} else if (duration >= 0) {
			BoxLiving.addCustomEffect(target, new wceTraceBeacon(cval, radiusFrom, radiusTo, Math.max(0,duration)), false);
		}
		return new SkillResult().push(ResultProperty.SUCCESS, true); //always succeed to not interrupt chain
	}
	
}
