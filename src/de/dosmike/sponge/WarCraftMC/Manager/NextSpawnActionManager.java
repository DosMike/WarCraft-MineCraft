package de.dosmike.sponge.WarCraftMC.Manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.api.entity.living.player.Player;

/** Reason for delaying action might be that you do not want a system to be abuse-able,
 * thus it could be a good idea to wait until the player changes the world or otherwise
 * triggers a spawn event. Of course if you feel it's apropriate at a certain time you
 * can also force execution by calling the onSpawn method in this manager. */
public class NextSpawnActionManager {
	private static Map<UUID, Set<Consumer<Player>>> queued = new HashMap<UUID, Set<Consumer<Player>>>();
	
	/** add a action to be done the next time this player respawns, will be removed on disconnect!
	 * @return true if the action was added, false if no such action (KeywordedConsumer) nor the action instance was already added */
	public static boolean add(Player player, Consumer<Player> action) {
		UUID at = player.getUniqueId();
		Set<Consumer<Player>> set = (queued.containsKey(at)?queued.get(at):new HashSet<Consumer<Player>>());
		if (contains(set, action)) return false;
		set.add(action);
		queued.put(at, set);
		return true;
	}
	/** add a action to be done the next time this player respawns, will be removed on disconnect!<br>
	 * if such action (KeywordedConsumer) or action instance was already added it will be replaced with the supplied one */
	public static void force(Player player, Consumer<Player> action) {
		UUID at = player.getUniqueId();
		Set<Consumer<Player>> set = (queued.containsKey(at)?queued.get(at):new HashSet<Consumer<Player>>());
		Consumer<Player> con = get(set, action);
		if (con!=null) set.remove(con);
		set.add(action);
		queued.put(at, set);
	}
	
	/** removes all action for a player, mostly when disconnecting */
	public static void removeAll(Player player) {
		queued.remove(player.getUniqueId());
	}
	
	/** removes a action by keyword */
	public static void remove(Player player, String key) {
		UUID at = player.getUniqueId();
		if (!queued.containsKey(at)) return;
		Set<Consumer<Player>> set = queued.get(at);
		Consumer<Player> con = get(set, key);
		if (con==null) return;
		set.remove(con);
		if (set.isEmpty())
			queued.remove(at);
		else
			queued.put(at, set);
	}
	/** removes a action by keyword */
	public static void remove(Player player, Consumer<Player> action) {
		UUID at = player.getUniqueId();
		if (!queued.containsKey(at)) return;
		Set<Consumer<Player>> set = queued.get(at);
		Consumer<Player> con = get(set, action);
		if (con==null) return;
		set.remove(con);
		if (set.isEmpty())
			queued.remove(at);
		else
			queued.put(at, set);
	}
	
	/** to be called when a player respawns. this does not have to be death, but can be a world change. */
	public static void onSpawn(Player player) {
		UUID at = player.getUniqueId();
		if (!queued.containsKey(at)) return;
		Set<Consumer<Player>> set = queued.remove(at);
		for (Consumer<Player> action : set) action.accept(player);
	}
	
	private static boolean contains(Set<Consumer<Player>> set, Consumer<Player> item) {
		if (!(item instanceof KeywordedConsumer)) return set.contains(item);
		KeywordedConsumer<Player> thisone = (KeywordedConsumer<Player>)item;
		for (Consumer<Player> other : set) if (thisone.sameKey(other)) return true;
		return false;
	}
	private static Consumer<Player> get(Set<Consumer<Player>> set, Consumer<Player> item) {
		if (!(item instanceof KeywordedConsumer)) return set.contains(item)?item:null;
		KeywordedConsumer<Player> thisone = (KeywordedConsumer<Player>)item;
		for (Consumer<Player> other : set) if (thisone.sameKey(other)) return other;
		return null;
	}
	private static Consumer<Player> get(Set<Consumer<Player>> set, String keyword) {
		for (Consumer<Player> other : set) if ((other instanceof KeywordedConsumer) && ((KeywordedConsumer<?>)other).isKey(keyword)) return other;
		return null;
	}
}
