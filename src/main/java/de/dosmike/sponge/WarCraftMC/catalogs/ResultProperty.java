package de.dosmike.sponge.WarCraftMC.catalogs;

public class ResultProperty<T> {
	
	public static final ResultProperty<Boolean> SUCCESS = new ResultProperty<>();
	public static final ResultProperty<Boolean> CANCEL_ACTION = new ResultProperty<>();
	/** stores the success amount for the previous skill in the chain for complex values */
	public static final ResultProperty<Double> CHAIN_VALUE = new ResultProperty<>();
	public static final ResultProperty<Double> MODIFY_DAMAGE = new ResultProperty<>();
	public static final ResultProperty<Boolean> COOLDOWN = new ResultProperty<>();

}
