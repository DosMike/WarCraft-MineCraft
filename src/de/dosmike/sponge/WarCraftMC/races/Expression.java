package de.dosmike.sponge.WarCraftMC.races;

import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;

import com.dolhub.tech.MathEval;

import de.dosmike.sponge.WarCraftMC.ManaPipe;
import de.dosmike.sponge.WarCraftMC.Profile;
import de.dosmike.sponge.WarCraftMC.RaceData;
import de.dosmike.sponge.WarCraftMC.exceptions.ResolveConditionException;

public class Expression {
	String string;
	static Random rng = new Random(System.currentTimeMillis());
    // create a JavaScript engine
//    static ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
	static MathEval engine = new MathEval();
    static Pattern FormulaPart = Pattern.compile("([=+*/-])((?:\\(.+?\\))|(?:[^=+*/()-]+))");
    static Pattern FormulaValue = Pattern.compile("((?:[0-9]+(?:\\.[0-9]+)?)|(?:(?:self|target)(?:\\.hp|\\.mana|\\.xp))|(\\$[0-9]+)|damage)");
	
	public Expression(String from) {
		string = from;
	}
	
	public Optional<?> resolve(ActionData data) {
//		System.out.print("["+string+"]");
		if (string.equals("self") && data.getSource().isPresent()) return data.getSource();
		if (string.equals("target") && data.getTarget().isPresent()) return data.getTarget();
		if (string.equals("item") && data.getItem().isPresent()) return data.getItem();
		if (string.equals("damage") && data.getDamage().isPresent()) return data.getDamage();
		if (string.equals("random")) return Optional.of(rng.nextDouble()*100.0);
		if (string.startsWith("self.") && data.getSource().isPresent()) {
			Player self = data.getSource().get();
			if (string.endsWith(".hp")) {
				Optional<Double> result = self.get(Keys.HEALTH);
				if (result.isPresent()) return Optional.of(result.get());
			} else if (string.endsWith(".mana")) { 
				return Optional.of(Double.valueOf(ManaPipe.getMana(self))); //i want numbers to always be double in skills 
			} else if (string.endsWith(".xp")) {
				Optional<RaceData> racedata = Profile.loadOrCreate(self).getRaceData();
				if (!racedata.isPresent()) return Optional.of(0.0);
				return Optional.of((double)racedata.get().getXP());
			} else return Optional.empty();
		}
		if (string.startsWith("target.") && data.getTarget().isPresent()) {
			Living target = data.getTarget().get();
			if (string.endsWith(".hp")) {
				Optional<Double> result = target.get(Keys.HEALTH);
				if (result.isPresent()) return Optional.of(result.get());
			} else if (string.endsWith(".mana")) {
				Double result = ((target instanceof Player)?ManaPipe.getMana((Player)target):rng.nextInt(10)); 
				return Optional.of(result); //i want numbers to always be double in skills 
			} else if (string.endsWith(".xp")) {
				if (!(target instanceof Player)) return Optional.of(0.0);
				Optional<RaceData> racedata = Profile.loadOrCreate((Player)target).getRaceData();
				if (!racedata.isPresent()) return Optional.of(0.0);
				return Optional.of(Double.valueOf(racedata.get().getXP()));
			} else return Optional.empty();
		}
		if (string.matches("[$][0-9]+")) {
			try {
				Integer i = Integer.parseInt(string.substring(1));
				Double value = data.getParammap()[i-1];
				return Optional.of(value);
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}
		if (string.charAt(0)=='=') {
//			System.out.print("Formula: ");
			String resultQuery = formula(string, data);
			if (resultQuery == null) {
				return Optional.empty();
			}
			try {
				if (engine == null) throw new RuntimeException("Unable to load Math Engine for math calculations!");
				Double result = engine.evaluate(resultQuery);
				return Optional.of( result );
			} catch (Exception e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}
		try {
			return Optional.of(Double.valueOf(string));
		} catch (Exception e) {
			return Optional.of(string);			
		}
	}
	
	static int stack = 0;
	String formula(String from, ActionData data) {
		//Using JavaXs JavaScript Engine to compute values by pre parsing elements as Expressions
		Matcher m = FormulaPart.matcher(from);
		String resultQuery="";
		while (m.find()) {
			if (m.group(1).equals("=")) {
				if (!resultQuery.isEmpty()) { //no formulas within formulas
					throw new ResolveConditionException("Stacked Formulas");
				}
			} else {
				resultQuery += m.group(1);
			}
			
			if (m.group(2).charAt(0)=='(') {
				//keep brackets while substitute variables within
				String subform = "="+m.group(2).substring(1, m.group(2).length()-1);
				subform = formula(subform, data);
				if (subform == null) return null;
				resultQuery += '(' + subform + ')';
			} else {
				Matcher n = FormulaValue.matcher(m.group(2));
				if (!n.matches()) {
					throw new ResolveConditionException('"' + m.group(2) + "\" Invalid!");
					//return null; //value won't be numeric, so not suitable for a formula
				}
				try {
					resultQuery += Double.parseDouble(m.group(2));
				} catch (NumberFormatException e) {
					Optional<?> exp = new Expression(m.group(2)).resolve(data);
					if (!exp.isPresent() || !(exp.get() instanceof Number)) {
						throw new ResolveConditionException("Couldn't resolve \"" + m.group(2) + '"');
//						return null;
					}
					resultQuery += exp.get();
				}
			}
		}
		return resultQuery;
	}
}
