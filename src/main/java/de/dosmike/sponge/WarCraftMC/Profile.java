package de.dosmike.sponge.WarCraftMC;

import de.dosmike.sponge.WarCraftMC.Manager.PermissionRegistry;
import de.dosmike.sponge.WarCraftMC.Manager.PlayerStateManager;
import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.events.ChangeRaceEvent;
import de.dosmike.sponge.WarCraftMC.events.GainXPEvent;
import de.dosmike.sponge.WarCraftMC.events.LevelUpEvent;
import de.dosmike.sponge.WarCraftMC.races.Action;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import de.dosmike.sponge.WarCraftMC.races.Race;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** gameProfile Wrapper - we're gonna work a lot with profiles, so this should make things easier for us
 * Methods that operate on disc level are synchronized */
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
	public static boolean isActive(Player player, Profile optionalProfile) {
		GameMode gm = player.get(Keys.GAME_MODE).get();
		if (gm.equals(GameModes.SPECTATOR)) return false;
		if (!PermissionRegistry.hasPermission(player, "active", true)) return false;
		//if (WarCraft.activePermission!=null && !player.hasPermission(WarCraft.activePermission)) return false;
		if (WarCraft.inactiveWorlds.contains(player.getWorld().getName())) return false;
		if (optionalProfile == null)
			optionalProfile = Profile.getIfOnline(player.getUniqueId()).orElse(null);
		//profile is present -> is online && has race
		return (optionalProfile != null && optionalProfile.getRaceData().isPresent());
	}
	
	private Profile(Player player) {
		playerID = player.getUniqueId();
		racedata = null;
	}
	
	public static boolean isLoaded(Player player) {
		return profileCache.containsKey(player.getUniqueId());
	}
	public synchronized void saveAndUnload() {
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
	public synchronized static Profile loadOrCreate(Player player) {
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
					WarCraft.tell(player, TextColors.GOLD, WarCraft.T().localText("player.error.load"));
					WarCraft.w("%s might have lost race progress!", player.toString());
				}
			} else {
				WarCraft.tell(player, TextColors.GOLD, WarCraft.T().localText("player.error.norace")
                                .replace("/racelist",  wcUtils.makeClickable("/racelist").build()));
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
	/** ensures the player is in wc-area and chose a race */
	public static Optional<Profile> getIfActive(Player player) {
		if (!player.isOnline() || !profileCache.containsKey(player.getUniqueId())) return Optional.empty();
		Profile prof = profileCache.get(player.getUniqueId()); //if online get the profile
		if (!isActive(player,prof)) return Optional.empty();
		return Optional.of(prof);
	}

	/** used for player manipulation, returns race data for all races regardless
	 * of whether the player is currently online */
	public synchronized static Map<Race, RaceData> dumpRaceData(User player) {

		Map<Race, RaceData> result = new HashMap<>();

		WarCraft.instance.getConfigDir().resolve("Profile").toFile().mkdirs();//becaus configurate does NOT create dirs
		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
				.setPath(WarCraft.instance.getConfigDir().resolve("Profile").resolve(player.getUniqueId().toString())).build();

		try {
			ConfigurationNode root = loader.load();
			RaceManager.getRaces().forEach(r->
					result.put(r, RaceData.loadOrCreate(r, root.getNode("races")))
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//use live data if available
		if (profileCache.containsKey(player.getUniqueId())) {
			RaceData data = profileCache.get(player.getUniqueId()).racedata;
			result.put(data.race, data);
		}

		return result;
	}
	/** used for player manipulation, this method forces a user profile to assume
	 * the data provided for the given race. if the user is online the cached
	 * race data are force updated as well in order to reflect changes done. */
	public synchronized static void forceRaceData(User user, RaceData data) {
		assert user!=null:"No user provided!";
		assert data!=null:"No data to save";

		if (profileCache.containsKey(user.getUniqueId())) {
			RaceData crace = profileCache.get(user.getUniqueId()).racedata;
			if (crace.race.equals(data.race)) {
				if (crace != data) { //if crace == data the player racedata were manipulated directly
					crace.levelXP = data.levelXP;
					crace.raceLevel = data.raceLevel;
					crace.skillCooldown = ArrayUtils.clone(data.skillCooldown);
					crace.skillPoints = data.skillPoints;
					crace.skillProgress = ArrayUtils.clone(data.skillProgress);
					crace.xp = data.xp;
				}
				return; //changes to race applied, will save on unload
			}
		}

		WarCraft.instance.getConfigDir().resolve("Profile").toFile().mkdirs();//becaus configurate does NOT create dirs
		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
				.setPath(WarCraft.instance.getConfigDir().resolve("Profile").resolve(user.getUniqueId().toString())).build();

		try {
			ConfigurationNode root = loader.load();
			data.Save(root.getNode("races"));
			loader.save(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Fire event to eventually cancel this and if not save race progress.<br>
	 * <i><b>NOTE:</b></i> This does not check the required level in case a<br>
	 * admin command wants to forcibly change a race later.
	 * @returns true if the switch was complete, false if the ChangeRaceEvent <br>was cancelled ot the level requirement failed */
//	public boolean switchRace(Race to, NamedCause cause) {
	public synchronized boolean switchRace(Race to) {
		if (racedata != null && racedata.getRace().getID().equals(to.getID())) return false; //no change so why bother
//		ChangeRaceEvent event = new ChangeRaceEvent(this, (racedata!=null?racedata.race:null), to, new EventCause(cause).get());
		ChangeRaceEvent event = new ChangeRaceEvent(this, (racedata!=null?racedata.race:null), to);
		/* copy this */Sponge.getEventManager().post(event); if (event.isCancelled()) return false;/* pasta that */

		WarCraft.instance.getConfigDir().resolve("Profile").toFile().mkdirs();//becaus configurate does NOT create dirs
		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
				.setPath(WarCraft.instance.getConfigDir().resolve("Profile").resolve(playerID.toString())).build();
		try {
			Optional<Player> profileOwner = Sponge.getServer().getPlayer(playerID);
			ConfigurationNode root = loader.load();
			if (racedata != null) {
				//clear all current race effects
				profileOwner.ifPresent(p->
						PlayerStateManager.resetPlayerFx(p, this)
				);

				racedata.Save(root.getNode("races"));
			}
			if (to != null) {
				racedata = RaceData.loadOrCreate(to, root.getNode("races"));
				Optional<Player> op = Sponge.getServer().getPlayer(playerID);
				if (op.isPresent()) {
					WarCraft.tell(op.get(), WarCraft.T().localText("player.racechange")
                            .replace("$race", to.getName()));
				}
//				XPpipe.processWarCraftXP(this, 0, new EventCause(cause).get());
				XPpipe.processWarCraftXP(this, 0);
//				pushXP(0, new EventCause(cause).get());

				//apply spawn effects from race
				profileOwner.ifPresent(p-> {
					ActionData actiondata = ActionData.builder(Action.Trigger.ONSPAWN)
							.setSelf(p)
							.build();
					racedata.fire(this, actiondata);
				});
			}
			else racedata = null;
			loader.save(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/** expecting a prefectly reasoned cause why this profile gains XP now*/
	public void pushXP(long XP) {
		if (XP < 0) throw new IllegalArgumentException("XP can't be nagative");
		if (racedata == null) return;
		if (XP > 0) {
			GainXPEvent event = new GainXPEvent(this, XP);
			/* copy this */Sponge.getEventManager().post(event); if (event.isCancelled()) return;/* pasta that */
			XP = event.getXP(); //get back XP in case it changed
		}
		
		//pass cause further in case of level up
		int levels = racedata.giveXP(XP); 
		if (levels > 0) {
			LevelUpEvent event2 = new LevelUpEvent(this, levels);
			/* copy this */Sponge.getEventManager().post(event2); if (event2.isCancelled()) return;/* pasta that */
			
			while (levels-->0) { racedata.levelUp(true); globalLevel++; } 
		}
	}

	/** This method completely deletes the player profile and all progress
	 * @return true if the profile was successfully deleted from disc */
	public synchronized boolean delete() {
		if (profileCache.containsKey(playerID))
			saveAndUnload();

		File profile = WarCraft.instance.getConfigDir().resolve("Profile").resolve(playerID.toString()).toFile();
		return profile.exists() && profile.delete();
	}
}
