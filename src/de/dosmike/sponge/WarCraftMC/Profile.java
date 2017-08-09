package de.dosmike.sponge.WarCraftMC;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.events.ChangeRaceEvent;
import de.dosmike.sponge.WarCraftMC.events.EventCause;
import de.dosmike.sponge.WarCraftMC.events.GainXPEvent;
import de.dosmike.sponge.WarCraftMC.events.LevelUpEvent;
import de.dosmike.sponge.WarCraftMC.races.Race;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

/** gameProfile Wrapper - we're gonna work a lot with profiles, so this should make things easier for us */
public class Profile {
	static Map<UUID, Profile> profileCache = new HashMap<>();
	
	UUID playerID;
	RaceData racedata;
	Integer storedXP=null, storedLevel=null; //used by XP pipe to restore XP and level - not saved, lost if server crashes (not my problem?)
	int globalLevel;
	public UUID getPlayerID() { return playerID; }
	
	public int getLevel() { return globalLevel; }
	public Optional<RaceData> getRaceData() { return racedata==null?Optional.empty():Optional.of(racedata); }
	
	/** determs if this player is currently actively able to take part in warcraft events.<br>
	 * This may includes data such as:<br>
	 * <li> Does the player have a race?
	 * <li> Is the player in a WC world?
	 * <li> Does the player have permissions to take part? */
	public boolean isActive(Player player) {
		if (racedata==null) return false;
		GameMode gm = player.get(Keys.GAME_MODE).get();
		if (gm.equals(GameModes.SPECTATOR)) return false;
		if (WarCraft.activePermission!=null && !player.hasPermission(WarCraft.activePermission)) return false;
		if (WarCraft.inactiveWorlds.contains(player.getWorld().getName())) return false;
		return true;
	}
	
	private Profile(Player player) {
		playerID = player.getUniqueId();
		racedata = null;
	}
	
	public static boolean isLoaded(Player player) {
		return profileCache.containsKey(player.getUniqueId());
	}
	public void saveAndUnload() {
		if (!profileCache.containsKey(playerID)) WarCraft.w("Player [%s] already unloaded", playerID.toString());
		
		WarCraft.instance.getConfigDir().resolve("Profile").toFile().mkdirs();//becaus configurate does NOT create dirs
		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
				.setPath(WarCraft.instance.getConfigDir().resolve("Profile").resolve(playerID.toString())).build();
		
		try {
			ConfigurationNode root = loader.load();
			root.getNode("level").setValue(globalLevel);
			if (racedata!=null) {
				root.getNode("activeRace").setValue(racedata.getRace().getID());
				racedata.Save(root.getNode("races"));
			}
			loader.save(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		profileCache.remove(playerID);
	}
	public static Profile loadOrCreate(Player player) {
		if (profileCache.containsKey(player.getUniqueId())) return profileCache.get(player.getUniqueId());
		
		WarCraft.instance.getConfigDir().resolve("Profile").toFile().mkdirs();//becaus configurate does NOT create dirs
		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
				.setPath(WarCraft.instance.getConfigDir().resolve("Profile").resolve(player.getUniqueId().toString())).build();
		
		Profile p = new Profile(player);
		try {
			ConfigurationNode root = loader.load();
			p.globalLevel = root.getNode("level").getInt(1);
			Optional<Race> r = RaceManager.getRace(root.getNode("activeRace").getString(""));
			if (r.isPresent()) {
				p.racedata = RaceData.loadOrCreate(r.get(), root.getNode("races"));
				if (p.racedata==null) {
					WarCraft.tell(player, TextColors.GOLD, "It looks like we were unable to restore your race progress...");
					WarCraft.w("%s might have lost race progress!", player.toString());
				}
			} else {
				WarCraft.tell(player, TextColors.GOLD, "Are not part of any race. Use ", wcUtils.makeClickable("/racelist").build(), " to make your choice.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		profileCache.put(player.getUniqueId(), p);
		return p;
	}
	/** To reduce stress on events we won't load offline players here!<br>
	 * Once you disconnect you'll loos all xp you would have got */
	public static Optional<Profile> getIfOnline(UUID player) {
		if (!Sponge.getServer().getPlayer(player).isPresent()) return Optional.empty();
		if (profileCache.containsKey(player)) return Optional.of(profileCache.get(player));
		return Optional.empty();
	}
	
	/** Fire event to eventually cancel this and if not save race progress.<br>
	 * <i><b>NOTE:</b></i> This does not check the required level in case a<br>
	 * admin command wants to forcibly change a race later.
	 * @returns true if the switch was complete, false if the ChangeRaceEvent <br>was cancelled ot the level requirement failed */
	public boolean switchRace(Race to, NamedCause cause) {
		if (racedata != null && racedata.getRace().getID().equals(to.getID())) return false; //no change so why bother
		ChangeRaceEvent event = new ChangeRaceEvent(this, (racedata!=null?racedata.race:null), to, new EventCause(cause).get());
		/* copy this */Sponge.getEventManager().post(event); if (event.isCancelled()) return false;/* pasta that */
		
		WarCraft.instance.getConfigDir().resolve("Profile").toFile().mkdirs();//becaus configurate does NOT create dirs
		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
				.setPath(WarCraft.instance.getConfigDir().resolve("Profile").resolve(playerID.toString())).build();
		try {
			ConfigurationNode root = loader.load();
			if (racedata != null) racedata.Save(root);
			if (to != null) {
				racedata = RaceData.loadOrCreate(to, root);
				Optional<Player> op = Sponge.getServer().getPlayer(playerID);
				if (op.isPresent()) WarCraft.tell(op.get(), "You are now part of the ", Text.of(TextColors.GOLD, to.getName()));
				XPpipe.processWarCraftXP(this, 0, new EventCause(cause).get());
//				pushXP(0, new EventCause(cause).get());
			}
			else racedata = null;
			loader.save(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/** expecting a prefectly reasoned cause why this profile gains XP now*/
	public void pushXP(long XP, Cause cause) {
		if (XP < 0) throw new IllegalArgumentException("XP can't be nagative");
		if (racedata == null) return;
		if (XP > 0) {
			GainXPEvent event = new GainXPEvent(this, XP, cause);
			/* copy this */Sponge.getEventManager().post(event); if (event.isCancelled()) return;/* pasta that */
			XP = event.getXP(); //get back XP in case it changed
		}
		
		//pass cause further in case of level up
		int levels = racedata.giveXP(XP); 
		if (levels > 0) {
			LevelUpEvent event2 = new LevelUpEvent(this, levels, cause);
			/* copy this */Sponge.getEventManager().post(event2); if (event2.isCancelled()) return;/* pasta that */
			
			while (levels-->0) { racedata.levelUp(true); globalLevel++; } 
		}
	}
}
