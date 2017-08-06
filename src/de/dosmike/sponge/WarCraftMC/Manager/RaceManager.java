package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.reflect.TypeToken;

import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.exceptions.DuplicateIDException;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.WarCraftMC.races.Skill;
import de.dosmike.sponge.WarCraftMC.serializer.RaceSerializer;
import de.dosmike.sponge.WarCraftMC.serializer.SkillSerializer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

public class RaceManager {
	private static WarCraft warCraft;
	public RaceManager(WarCraft link) {
		warCraft = link;
	}
	
	//warcraft vars
	static Map<String, Race> races = new HashMap<>();

	/** try to get a race by it's name */
	public static Optional<Race> getRace(String id) {
		id = id.toLowerCase();
		if (races.containsKey(id)) return Optional.of(races.get(id));
		else return Optional.empty();
	}
	
	/** try to get a race by it's name */
	public static Optional<Race> getRaceByName(String name) {
		for (Race r : races.values()) if (r.getName().equalsIgnoreCase(name)) return Optional.of(r); return Optional.empty();
	}

	public static Collection<Race> getRaces() {
		return races.values();
	}

	public static void loadRaces() {
		TypeSerializerCollection customSerializer = TypeSerializers.getDefaultSerializers().newChild();
		customSerializer.registerType(TypeToken.of(Skill.class), new SkillSerializer());
		customSerializer.registerType(TypeToken.of(Race.class), new RaceSerializer());
		ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(customSerializer);
		try {
			ConfigurationNode root = warCraft.getRaceConfig().load(options);
			Collection<Race> races = root.getNode("races").getValue(new TypeToken<List<Race>>(){}, new LinkedList<Race>());
			for (Race race : races) {
				try {
					registerRace(race);
				} catch (DuplicateIDException did) {
					WarCraft.w(did.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			WarCraft.w("Could not load races!");
		}
	}

	public static void registerRace(Race r) throws DuplicateIDException {
		if (races.containsKey(r.getID())) throw new DuplicateIDException("A race with ID "+r.getID()+" is already Registered!");
		races.put(r.getID(), r);
	}
	
}
