package de.dosmike.sponge.WarCraftMC.races;

import java.text.ParseException;
import java.util.Optional;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.exceptions.ExecuteActionException;
import de.dosmike.sponge.WarCraftMC.exceptions.PrepareSkillActionException;

public class Action {
	private String from; //used for fixing your messed up config!
	
	public static enum Trigger { ACTIVE1, ACTIVE2, ACTIVE3, ULTIMATE, ACTIVE_INVALID, ONSNEAK, ONJUMP, ONATTACK, ONHIT, ONBLOCK, ONUNBLOCK, ONUSEITEM, ONSPRINT, ONDROP, ONPICKUP, ONSPAWN, ONDEATH };
	//Event
	Trigger event;
	//Condition
	Optional<Condition> condition;
	//Output[]
	Output[] outputs;
	
	/** First checks if the trigger is fitting for this action, or returns empty<br>
	 * Then it checks all conditions, if the conditions fail a result with SUCCESS property false will be returned<br>
	 * If everything passes it will fire all outputs in order until the first output returns a SUCCESS property false
	 * @return a skill result if this action was triggered */
	public Optional<SkillResult> fire(ActionData data) throws ExecuteActionException {
//		WarCraft.l(event+" vs "+data.getTrigger());
		if (event != data.getTrigger()) return Optional.empty();
		SkillResult result = new SkillResult();
		try {
			if (!condition.isPresent() || condition.get().evaluate(data)) {
				for (Output output : outputs) {
					SkillResult r = output.fire(data, result);
					result.filter(r, ResultProperty.SUCCESS);
					if (r.get(ResultProperty.SUCCESS).contains(false)) { result.push(ResultProperty.SUCCESS, false); break; }
				}
			} else {
				result.push(ResultProperty.SUCCESS, false);
			}
		} catch (Exception e) {
			throw new ExecuteActionException("Could not execute action <<"+from+">>", e);
//			e.printStackTrace();
		}
		/** remove all values for the outputs, they are no longer important and put our success result */
		return Optional.of(result.push(ResultProperty.SUCCESS, true));
	}
	
	private Action() {}
	public static Action fromString(String action) throws ParseException, PrepareSkillActionException {
		Action result = new Action();
		result.from = action;
		//WORD (...) WORD WORD | WORD WORD
		String event = action.substring(0, action.indexOf(' ')).toUpperCase();
		if (!event.startsWith("ON") && !event.equals("ACTIVE")) throw new ParseException(event + " is not a valid event", 0);
		if (event.equals("ACTIVE")) event = "ACTIVE_INVALID"; // template
		action = action.substring(action.indexOf(' ')).trim();
		if (action.charAt(0)!='(') throw new ParseException("Missing conditions for skill action "+ action, event.length());
		int pos = 1;
		int depth = 1;
		for (;depth>0 && pos<action.length();pos++) {
			if (action.charAt(pos)=='(') depth++;
			else if (action.charAt(pos)==')') depth--;
		}
		if (depth > 0) throw new ParseException("No closing-brackets for conditions on skill action "+ action, 0);
		String condition = action.substring(0, pos);
		action = action.substring(pos).trim();
		String[] outputs = action.split("\\|");
		
		try {
			result.event = Trigger.valueOf(event.toUpperCase());
			WarCraft.l("  Trigger on "+event);
			Condition cond = Condition.fromString(condition);
			result.condition = cond==null?Optional.empty():Optional.of(cond);
			result.outputs = new Output[outputs.length];
			for (int i = 0; i < outputs.length; i++) {
				result.outputs[i] = Output.fromString(outputs[i].trim());
			}
			return result;
		} catch (Exception e) {
			throw new PrepareSkillActionException("Chould not prepare action "+action, e);
		}
	}
}
