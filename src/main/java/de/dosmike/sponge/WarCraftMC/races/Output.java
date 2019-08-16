package de.dosmike.sponge.WarCraftMC.races;

import de.dosmike.sponge.WarCraftMC.Manager.SkillManager;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.exceptions.PrepareOutputException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Output {
	Method method;
	Expression[] params;
	
	Output() {
		
	}
	
	/** parse a single MethodName Args... string as CLI string (quoted args possible), for readability using ' instead of " 
	 * @throws ParseException */
	public static Output fromString(String from) throws PrepareOutputException {
		List<String> args = new LinkedList<>();
		try {
		boolean inQuotes=false; char h='\'';
		String word="";
		for (int i=0; i<from.length(); i++) {
			char c = from.charAt(i);
			if (c==h) {
				if (word.isEmpty() && !inQuotes) {
					inQuotes = true;
				} else if (inQuotes) {
					if (i==from.length()-1) { /*args.add(word);*/ inQuotes=false; }
					else if (from.charAt(i+1)==' ') { inQuotes = false; }
					else if (from.charAt(i+1)==h) { word += h; i++; }
					else throw new ParseException("Missplaces ' for \""+from+"\" around position "+i, i);
				} else throw new ParseException("Missplaces ' for \""+from+"\" around position "+i, i);
			} else if (c==' ') {
				if (inQuotes) { word+=c; }
				else { args.add(word); word=""; }
			} else {
				word+=c; 
			}
		}
		if (inQuotes) throw new ParseException("No end-quote for \""+from+"\"", from.length());
		if (!word.isEmpty()) args.add(word);
		if (args.isEmpty()) throw new ParseException("Skill action has no Output", 0);
		} catch (Exception e) {
			throw new PrepareOutputException("Error while parsing skill action", e);
		}
		
		Output result = new Output();
//		WarCraft.l("Output Args: "+Arrays.toString(args.toArray()));
		Method m = SkillManager.getSkill(args.get(0));
		if (m==null) throw new PrepareOutputException("No such skill action \""+args.get(0)+"\" at "+from);
		if (m.getParameterCount() != args.size()-1) throw new PrepareOutputException("Argument missmatch \""+args.get(0)+"\" - expected "+m.getParameterCount()+", got "+(args.size()-1));
		result.method = m;
		result.params = new Expression[args.size()-1];
		for (int i = 1; i < args.size(); i++) {
			result.params[i-1]=new Expression(args.get(i));
		}
		return result;
	}
	
	/** Fires this output of the holding skill.
	 * Throws exceptions if Expressions can't resolve */
	public SkillResult fire(ActionData data, SkillResult result) {
		Object[] paraminstance = new Object[params.length];
		for (int i = 0; i < params.length; i++) {
			paraminstance[i] = params[i].resolve(data).orElseThrow(()-> {
				return new RuntimeException("Error while resolving parameter for "+method.getName());
			});
			if (paraminstance[i] instanceof String) {
				String val = ((String)paraminstance[i]);
				if (val.contains("$self")) val=val.replace("$self", optionalResolve(data.getSource(), ">SELF<"));
				if (val.contains("$target")) val=val.replace("$target", optionalResolve(data.getTarget(), ">TARGET<"));
				if (val.contains("$item")) val=val.replace("$item", optionalResolve(data.getItem(), ">ITEM<"));
				if (val.contains("$damage")) val=val.replace("$damage", optionalResolve(data.getDamage(), ">DAMAGE<"));
				if (val.contains("$dmgmod")) val=val.replace("$dmgmod", damageModifier(result));
				paraminstance[i]=val;
			}
		}
		try {
			return (SkillResult)method.invoke(null, paraminstance);
		} catch (Exception e) {
			throw new RuntimeException("Error while invoking skill action "+(method==null?"NOTHING":method.getName())+":"+Arrays.toString(paraminstance), e);
		}
	}
	<T> String optionalResolve(Optional<T> optional, String fallback) {
		if (!optional.isPresent()) return fallback;
		T value = optional.get();
		if (value instanceof Player) return ((Player)value).getName();
		if (value instanceof Living) return ((Living)value).getTranslation().get();
		if (value instanceof ItemStack) {
			ItemStack item = (ItemStack) value;
			if (item.getKeys().contains(Keys.DISPLAY_NAME)) return item.get(Keys.DISPLAY_NAME).get().toPlain();
			return item.getType().getTranslation().get();
		}
		return value.toString();
	}
	String damageModifier(SkillResult previous) {
		Double damage=0.0; for (Double d : previous.get(ResultProperty.MODIFY_DAMAGE)){damage+=d;}
		return String.format("%.2f", damage);
	}
}
