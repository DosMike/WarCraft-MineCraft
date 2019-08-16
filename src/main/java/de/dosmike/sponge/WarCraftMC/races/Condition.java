package de.dosmike.sponge.WarCraftMC.races;

import de.dosmike.sponge.WarCraftMC.exceptions.ConditionFailedException;
import de.dosmike.sponge.WarCraftMC.exceptions.PrepareConditionException;
import de.dosmike.sponge.WarCraftMC.races.ConditionList.Conjunction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Condition {
	public abstract boolean evaluate(ActionData data) throws ConditionFailedException;
	
	static Pattern tokenizer = Pattern.compile("((?:\\(.+\\))|(?:[^&|()]+))(?:([&|])(.+))?"); 
	public static Condition fromString(String from) throws PrepareConditionException {
		from=from.trim();
		if (from.charAt(0)=='(' && from.charAt(from.length()-1)==')') {
			ConditionList result = new ConditionList();
			from = from.substring(1, from.length()-1);
			if (from.isEmpty()) return null;
			Matcher m = tokenizer.matcher(from);
			if (m.matches()) {
				if (m.group(1)!=null && m.group(2)==null && m.group(3)==null) { //only 1 filled
					result.children = new Condition[1];
					result.children[0] = fromString(m.group(1));
				} else if (m.group(1)!=null && m.group(2)!=null && m.group(3)!=null) { //all 3 filled
					result.children = new Condition[2];
					if (m.group(2).charAt(0)=='&')
						result.conjunction = Conjunction.AND;
					else
						result.conjunction = Conjunction.OR;
					result.children[0] = fromString(m.group(1));
					result.children[1] = fromString(m.group(3));
					if (result.children[0] == null || result.children[1] == null)
						throw new PrepareConditionException("Empty brackets, condition required at "+from);
				} else throw new PrepareConditionException("Found conditin conjunction without condition CONDITION &/ EOT at "+from);
			} else throw new PrepareConditionException("Unknown condition pattern for "+from);
			return result;
		} else if (from.charAt(0)=='(' || from.charAt(from.length()-1)==')'){
			throw new PrepareConditionException("Missin brackets");
		} else {
			return new ConditionComparator(from);
		}
	}
}
