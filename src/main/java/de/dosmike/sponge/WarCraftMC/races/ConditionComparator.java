package de.dosmike.sponge.WarCraftMC.races;

import de.dosmike.sponge.WarCraftMC.exceptions.ConditionFailedException;
import de.dosmike.sponge.WarCraftMC.exceptions.PrepareConditionException;
import de.dosmike.sponge.WarCraftMC.wcUtils;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public class ConditionComparator extends Condition {
	public static enum CompareMethod { ABOVE, BELOW, TEAMMATE, OPPONENT, OFTYPE, NOTTYPE }
	
	String from;
	Expression a, b;
	CompareMethod method;
	
	ConditionComparator(String from) throws PrepareConditionException {
		this.from = from;
		try {
			String[] parts = from.trim().replace("  ", " ").split(" ");
			method = CompareMethod.valueOf(parts[1].toUpperCase());
			this.a = new Expression(parts[0]);
			this.b = new Expression(parts[2]);
		} catch (Exception e) {
			throw new PrepareConditionException("Invalid condition: "+from);
		}
	}
	
	@Override
	public boolean evaluate(ActionData data) throws ConditionFailedException {
		Optional<?> vala, valb;
		try {
			vala = a.resolve(data);
			valb = b.resolve(data);
			if (!vala.isPresent() || !valb.isPresent()) {
				throw new ConditionFailedException(String.format("Unable to resolve expression in condition <%s %s %s>", a.string, method, b.string));
			}
		} catch (Exception e) {
			throw new ConditionFailedException("Could not resolve condition values \""+from+"\": ", e);
		}
		Object x = vala.get(), y = valb.get();
		switch (method) {
		case ABOVE:
			if (x instanceof Number && y instanceof Number) {
//				WarCraft.l("Compare ABOVE: "+x+", "+y);
				return ((Number)x).doubleValue()>((Number)y).doubleValue();
			} else throw new ConditionFailedException(String.format("Can't compare non-numerics with ABOVE: [%s] [%s]", x, y));
		case BELOW:
			if (x instanceof Number && y instanceof Number) {
//				WarCraft.l("Compare BELOW: "+x+", "+y);
				return ((Number)x).doubleValue()<((Number)y).doubleValue();
			} else throw new ConditionFailedException(String.format("Can't compare non-numerics with BELOW: [%s] [%s]", x, y));
		case TEAMMATE:
			if (x instanceof Living && y instanceof Living) return wcUtils.sameTeamAs(data.getSource().get(), data.getTarget().get());
			else throw new ConditionFailedException("Can only compare teams for players");
		case OPPONENT:
			if (x instanceof Living && y instanceof Living) return !wcUtils.sameTeamAs(data.getSource().get(), data.getTarget().get());
			else throw new ConditionFailedException("Can only compare teams for players");
		case OFTYPE:
			if (x instanceof ItemStack && y instanceof String) return wcUtils.ofType(data.getItem().get(), (String)y);
			else throw new ConditionFailedException("Syntax for OFTYPE is: item oftype TYPENAME");
		case NOTTYPE:
			if (x instanceof ItemStack && y instanceof String) return !wcUtils.ofType(data.getItem().get(), (String)y);
			else throw new ConditionFailedException("Syntax for NOTTYPE is: item nottype TYPENAME");
		default: //should never hit
			throw new ConditionFailedException("Unknown compairson type " + method);
		}
	}
}
