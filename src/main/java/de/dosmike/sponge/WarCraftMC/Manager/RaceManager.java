package de.dosmike.sponge.WarCraftMC.Manager;

import com.google.common.reflect.TypeToken;
import de.dosmike.sponge.WarCraftMC.WarCraft;
import de.dosmike.sponge.WarCraftMC.exceptions.DuplicateIDException;
import de.dosmike.sponge.WarCraftMC.races.Race;
import de.dosmike.sponge.WarCraftMC.races.Skill;
import de.dosmike.sponge.WarCraftMC.serializer.RaceSerializer;
import de.dosmike.sponge.WarCraftMC.serializer.SkillSerializer;
import de.dosmike.sponge.WarCraftMC.wcUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;

import java.util.*;

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
	public static Optional<Race> getRaceByName(String name, Player locale) {
		for (Race r : races.values())
			if (r.getName(locale).equalsIgnoreCase(name))
				return Optional.of(r);
		return Optional.empty();
	}

	public static Collection<Race> getRaces() {
		return races.values();
	}

	@SuppressWarnings("serial")
	private static TypeToken<List<Race>> ttlr = new TypeToken<List<Race>>(){}; 
	
	public static void loadRaces() {
		TypeSerializerCollection customSerializer = TypeSerializers.getDefaultSerializers().newChild();
		customSerializer.registerType(TypeToken.of(Skill.class), new SkillSerializer());
		customSerializer.registerType(TypeToken.of(Race.class), new RaceSerializer());
		ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(customSerializer);
		races.values().forEach(PermissionRegistry::unregister);
		try {
			ConfigurationNode root = warCraft.getRaceConfig().load(options);
			Collection<Race> races = root.getNode("races").getValue(ttlr, new LinkedList<Race>());
			for (Race race : races) {
				try {
					registerRace(race);
				} catch (DuplicateIDException did) {
					WarCraft.w(did.getMessage());
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
			
			StringBuilder simpleTrace = new StringBuilder("Unable to load your RACES.CONF");
			wcUtils.superSimpleTrace(simpleTrace, e, "\n");
			System.err.println(simpleTrace.toString());
			
			WarCraft.w("Could not load races!");
		}
	}

	public static void registerRace(Race r) throws DuplicateIDException {
		if (races.containsKey(r.getID())) throw new DuplicateIDException("A race with ID "+r.getID()+" is already Registered!");
		races.put(r.getID(), r);

		PermissionRegistry.register(r,
				"wc.race.change."+r.getID(),
				Text.of("Allow the player to change to race "+r.getName(Sponge.getServer().getConsole())),
				PermissionDescription.ROLE_USER);
	}

}
