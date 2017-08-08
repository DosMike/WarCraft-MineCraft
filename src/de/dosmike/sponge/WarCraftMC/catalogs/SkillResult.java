package de.dosmike.sponge.WarCraftMC.catalogs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SkillResult {
	public class ResultPropertyHolder<X> {
		ResultProperty<X> key;
		X value;
		public ResultPropertyHolder(ResultProperty<X> k, X v) {
			key=k; value=v;
		}
		ResultProperty<X> getKey() { return key; }
		X getValue() { return value; }
	}
	
	List<ResultPropertyHolder<?>> res = new LinkedList<>(); 
	
	public <T> SkillResult push(ResultProperty<T> property, T value) {
		res.add(new ResultPropertyHolder<T>(property, value));
		return this;
	}
	public void push(SkillResult other) {
		res.addAll(other.res);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Collection<T> get(ResultProperty<T> key) {
		List<T> result = new LinkedList<>();
		for (ResultPropertyHolder<?> h : res)
			if (h.getKey().equals(key)) 
				result.add(((ResultPropertyHolder<T>) h).getValue());
		return result;
	}
	
	/** Adds all ResultProperty keys from the other result to this skill result but key */
	public <T> SkillResult filter(SkillResult other, ResultProperty<T> filter) {
		for (ResultPropertyHolder<?> h : res)
			if (!h.getKey().equals(filter)) 
				res.add(h);
		return this;
	}
	
	public SkillResult() {}
	
}
