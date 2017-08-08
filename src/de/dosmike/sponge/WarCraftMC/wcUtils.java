package de.dosmike.sponge.WarCraftMC;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class wcUtils {
	public static boolean sameTeamAs(Living a, Living b) {
		if (a instanceof Player && b instanceof Player) {
			Player x = (Player)a;
			Player y = (Player)b;
			Optional<Team> m = x.getScoreboard().getTeam(x.getName());
			if (m.isPresent()) return m.get().getMembers().contains(y.getName());
		}
		return false;
	}
	
	/** Check anything that has a type if the type is desiredType by string compairson.<br>
	 * Let's say for a onPickup filter the desired type shall be "minecraft:dirt" for a ItemStack.<br>
	 * The object we get is a ItemStack, and the type is dirt, so we reflectively call getType for the ItemStack and compare it's name to the desired type.
	 * @param oneOf the Object to compare the Catalogable Type with
	 * @param desiredType the name the Catalogable Type should match
	 * @return true if the name matches, case insensitive
	 * @throws IllegalArgumentException if oneOf does not have a getType() function */
	public static boolean ofType(Object oneOf, String desiredType) {
		try {
			Method getType = oneOf.getClass().getDeclaredMethod("getType");
			CatalogType type = (CatalogType) getType.invoke(oneOf);
			return type.getName().equalsIgnoreCase(desiredType);
		} catch (Exception e) {
			throw new IllegalArgumentException("One of " + oneOf.getClass().getSimpleName() + " does not have types!", e);
		}
	}
	
	/** makes a text clickable by performing the command on click, has underline style, and color reset by default.
	 * @return A Text.Builder for further editing */
	public static Text.Builder makeClickable(String text, String command) {
		return Text.builder(text).style(TextStyles.UNDERLINE).color(TextColors.RESET).onClick(TextActions.runCommand(command)).append(Text.of(TextColors.RESET, TextStyles.RESET));
	}
	/** makes a text clickable by performing the command on click, has underline style, and color reset by default.
	 * @return A Text.Builder for further editing */
	public static Text.Builder makeClickable(String text, Consumer<CommandSource> command) {
		return Text.builder(text).style(TextStyles.UNDERLINE).color(TextColors.RESET).onClick(TextActions.executeCallback(command)).append(Text.of(TextColors.RESET, TextStyles.RESET));
	}
	/** thought especially for commands, so text will be executed as command on click
	 * @return A Text.Builder for further editing */
	public static Text.Builder makeClickable(String text) {
		return Text.builder(text).style(TextStyles.UNDERLINE).color(TextColors.RESET).onClick(TextActions.runCommand(text)).append(Text.of(TextColors.RESET, TextStyles.RESET));
	}
	/** thought especially for commands, so text will be executed as command on click
	 * @return A Text.Builder for further editing */
	public static Text.Builder makeClickable(Text text, String command) {
		return Text.builder().append(text).onClick(TextActions.runCommand(command)).append(Text.of(TextColors.RESET, TextStyles.RESET));
	}
	/** thought especially for commands, so text will be executed as command on click
	 * @return A Text.Builder for further editing */
	public static Text.Builder makeClickable(Text text, Consumer<CommandSource> command) {
		return Text.builder().append(text).onClick(TextActions.executeCallback(command)).append(Text.of(TextColors.RESET, TextStyles.RESET));
	}
	public static Text resetText = Text.of(TextColors.RESET, TextStyles.RESET);
	
	/** tries to get element by name to upper case as all fields are upper case */
	@SuppressWarnings("unchecked")
	public static <X> Optional<X> getTypeByName(String type, Class<?> catalog) {
		try {
			Field f = catalog.getDeclaredField(type.toUpperCase());
			return Optional.of((X)f.get(null));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	/** formats a simplified stacktrace into the string builder without java soruces attached */
	public static void superSimpleTrace(StringBuilder sb, Throwable t, String s) {
		sb.append(s+"> "+t.getClass().getSimpleName()+": "+t.getMessage());
		Throwable c = t.getCause();
		if (c!=null) superSimpleTrace(sb, c, s+" ");
	}
}
