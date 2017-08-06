package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.Comparator;

import de.dosmike.sponge.WarCraftMC.races.Race;

public class RaceSorter implements Comparator<Race> {
	@Override
	public int compare(Race o1, Race o2) {
		int v = Integer.compare(o1.getRequiredLevel(), o2.getRequiredLevel());
		if (v==0) v = o1.getName().compareTo(o2.getName());
		return v;
	}
}
