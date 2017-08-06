package de.dosmike.sponge.WarCraftMC.races;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.exceptions.ConditionFailedException;

public class ConditionList extends Condition {
	public static enum Conjunction { AND, OR; }
	Conjunction conjunction;
	Condition[] children;
	
	ConditionList() {
	}
	
	@Override
	public boolean evaluate(ActionData data) throws ConditionFailedException {
		if (conjunction == Conjunction.AND) {
			for (Condition child : children) {
				Boolean evaluate = child.evaluate(data);
				if (child instanceof ConditionComparator) WarCraft.l(((ConditionComparator)child).from + "=" + evaluate);
				if (!evaluate) return false;
			}
			return true;
		} else {
			for (Condition child : children) {
				Boolean evaluate = child.evaluate(data);
				if (child instanceof ConditionComparator) WarCraft.l(((ConditionComparator)child).from + "=" + evaluate);
				if (evaluate) return true;
			}
			return false;
		}
	}
}
