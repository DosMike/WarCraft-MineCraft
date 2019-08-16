package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.function.Consumer;

/** keyworded consumer to e.g. allow only one consumer with the same keyword in a list */
public abstract class KeywordedConsumer<T> implements Consumer<T> {

	String key;
	@SuppressWarnings("unused")
	private KeywordedConsumer(){}
	public KeywordedConsumer(String keyword) {
		key=keyword;
	}

	/** keys are case insensitive */
	public boolean sameKey(Consumer<?> other) {
		if (!(other instanceof KeywordedConsumer)) return false;
		String otherKey = ((KeywordedConsumer<?>)other).key;
		return (key==null?otherKey==null:key.equalsIgnoreCase(otherKey));
	}
	
	public boolean isKey(String key) { return (key==null?this.key==null:key.equalsIgnoreCase(this.key)); }
}
