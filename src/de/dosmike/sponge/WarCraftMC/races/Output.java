package de.dosmike.sponge.WarCraftMC.races;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.dosmike.sponge.WarCraftMC.Skills;
import de.dosmike.sponge.WarCraftMC.exceptions.PrepareOutputException;

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
					if (i==from.length()-1) { args.add(word); inQuotes=false; }
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
		} catch (Exception e) {
			throw new PrepareOutputException("Error while parsing skill action", e);
		}
		
		Output result = new Output();
		Method m = Skills.getSkill(args.get(0));
		if (m==null) throw new PrepareOutputException("No such skill \""+args.get(0)+"\" at "+from);
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
	public void fire(ActionData data) {
		Object[] paraminstance = new Object[params.length];
		for (int i = 0; i < params.length; i++) paraminstance[i] = params[i].resolve(data).orElseThrow(()-> {
			return new RuntimeException("Error while resolving parameter for "+method.getName());
		});
		try {
			method.invoke(null, paraminstance);
		} catch (Exception e) {
			throw new RuntimeException("Error while invoking skill action "+(method==null?"NOTHING":method.getName())+":"+Arrays.toString(paraminstance), e);
		}
	}
}
