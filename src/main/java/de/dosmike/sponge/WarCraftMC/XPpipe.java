package de.dosmike.sponge.WarCraftMC;

import de.dosmike.sponge.WarCraftMC.events.GainXPEvent;
import de.dosmike.sponge.WarCraftMC.events.LevelUpEvent;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class XPpipe {
	/**
	 * This XP multiplier table can be used to exclude xp reward for e.g. peacful mobs
	 * and make rewards for more duifficult mobs even higher
	 */
	static Map<EntityType, Double> xpMultipliers = new HashMap<>();
	/** returns the global multiplier for xp * entity multiplier if applicable */
	public static Double getMultiplierOr1(EntityType entity) {
		if (xpMultipliers.containsKey(entity))
			return globalMultiplier*xpMultipliers.get(entity);
		else
			return globalMultiplier;
	}
	/** this multiplier can be used for e.g. Double XP weekends and stuff */
	static Double globalMultiplier = 1.0;
	
	/** what to do with the vanilla XP system.
	 * <li>If you choose Ignore the WC XP and vanilla XP will be separated
	 * <li>If you choose reflect the WC will not give XP, but the vanilla XP and level will be used to level your race.<br>
	 * Using this option will change your level on change race / active state.
	 * <li>Replace the WC XP system will use the vanilla system to display your XP progress and level.<br>
	 * You XP/level before WC will be restored as you leave WC */
	public static enum Mode {
		/** Ignores the vanilla XP and uses a completely separated custom system */
		IGNORE,
		/** Reflect the vanilla XP into warcraft, effectively using the vanilla system */
		REFLECT,
		/** Replacing the vanilla XP system with the custom system and push levels into vanilla */
		REPLACE
	}
	
	static Mode mode;
	public static Mode getMode() {
		return mode;
	}
	
	public static void processWarCraftXP(Profile profile, long amount) {
//		if (!profile.getRaceData().isPresent()) return; //is present, checked by caller
		
		switch (mode) {
		case REFLECT: break; //we use the vanilla system, ignore this
		case REPLACE:
			Optional<Player> gottenXP = Sponge.getServer().getPlayer(profile.getPlayerID());
			RaceData rd = profile.getRaceData().get();
			
			profile.pushXP(amount);
			
			int l = rd.getLevel();
			int xp = (int)rd.getXP();
			int maxxp = (int) (rd.getLevel()==rd.getRace().getMaxLevel() ? xp : rd.getLevelXp());
			if (maxxp<1) maxxp=xp;
			if (gottenXP.isPresent()) setXPbar(gottenXP.get(), l, (float)xp/(float)maxxp);
			break;
		default:
			profile.pushXP(amount);
		}
	}
	/*public static void processWarCraftLevel(Player player, Profile profile, int amount, Cause cause) {
		if (!profile.getRaceData().isPresent()) return;
		RaceData rd = profile.getRaceData().get();
		switch (mode) {
		case REFLECT: break;
		case REPLACE: {
			int l = rd.getLevel();
			int xp = (int)rd.getXP();
			int maxxp = (int) (rd.getLevel()==rd.getRace().getMaxLevel() ? xp : rd.getRace().getLevelXp(l));
			setXPbar(player, l, (float)xp/(float)maxxp);
		}
		default:
		}
	}*/
	/** returns whether to cancel the original event 
	 * @return true to cancel the original event */
	public static boolean processVanillaXP(Player player, Profile profile) {
		assert profile != null : "No profile supplied";
		if (!profile.getRaceData().isPresent()) return false;
		RaceData data = profile.getRaceData().get();
		switch(mode) {
		case REFLECT:
			int l = player.get(Keys.EXPERIENCE_LEVEL).get();
			int xp = player.get(Keys.EXPERIENCE_SINCE_LEVEL).get();
			int maxxp = player.get(Keys.EXPERIENCE_FROM_START_OF_LEVEL).get();
			double progress = (double)xp/(double)maxxp;
			int wcl = data.getLevel();
			int wcxp = (int) data.getXP();
			int wcmaxxp = (int) (data.getLevel()==data.getRace().getMaxLevel() ? xp : data.getLevelXp());
			if (wcmaxxp<1) wcmaxxp=wcxp;
			double wcprogress = (double)wcxp/(double)wcmaxxp;
			
			//can't reduce level in wc, that's just not supportet. would require to unskill stuff and dupe SP... not gud
			//what happened here is that someone probably was at a enchantment table and lost levels through that, ignore this
			if (l<wcl) return false; 
			if (l==wcl && progress <= wcprogress) {
				return false; //this would equal a xp drain - not implemented yet //2019: well no, but actually yes
			}
			//the target wc level we have after this 
			int tl = (l>data.getRace().getMaxLevel() ? data.getRace().getMaxLevel() : l);
			//the target wc XP we have after this
			long txp = (long)(wcmaxxp*progress);
			
			int lu = tl-wcl; //the amount of levels we got
			
			//update WC profile data
			if (txp>wcxp) {
				GainXPEvent event = new GainXPEvent(profile, txp-wcxp);
				Sponge.getEventManager().post(event); if (event.isCancelled()) return true;
			}
			if (tl>wcl) {
				LevelUpEvent event = new LevelUpEvent(profile, lu);
				Sponge.getEventManager().post(event); if (event.isCancelled()) return true;
			}
			
			data.raceLevel=tl;
			data.xp=txp;
			data.skillPoints+=lu; //give 1 SP per levelup
			
			break;
		case REPLACE:
//			Optional<WarCraft> wcupdate = cause.first(WarCraft.class);
			Optional<WarCraft> wcupdate = Sponge.getCauseStackManager().getCurrentCause().first(WarCraft.class);
			return !wcupdate.isPresent(); //replacing the system means we do not allow changes unless warcraft is the cause
		default:
		}
		return false;
	}
	
	public static void archiveVanilla(Player player, Profile profile) {
		if (mode == Mode.IGNORE) return;
		if (profile.storedLevel==null) {
			profile.storedLevel = player.get(Keys.EXPERIENCE_LEVEL).get();
			profile.storedXP = player.get(Keys.EXPERIENCE_SINCE_LEVEL).get();
			
			if (profile.getRaceData().isPresent()) {
				RaceData rd = profile.getRaceData().get();
				int l = rd.getLevel();
				int xp = (int)rd.getXP();
				int maxxp = (int) (rd.getLevel()==rd.getRace().getMaxLevel() ? xp : rd.getLevelXp());
				if (maxxp<1) maxxp=xp;
				setXPbar(player, l, (double)xp/(double)maxxp);
			}
		}
	}
	public static void restoreVanilla(Player player, Profile profile) {
		if (mode == Mode.IGNORE) return;
		if (profile.storedLevel!=null) {
			player.offer(Keys.EXPERIENCE_LEVEL, profile.storedLevel);
			player.offer(Keys.EXPERIENCE_SINCE_LEVEL, profile.storedXP);
			profile.storedLevel=null;
			profile.storedXP=null;
		}
	}
	
	public static void setXPbar(Player player, int Level, double Progress) {
		player.offer(Keys.EXPERIENCE_LEVEL, Level);
		int maxxp = player.get(Keys.EXPERIENCE_FROM_START_OF_LEVEL).get();
		int newxp = (int) (Progress*maxxp);
		player.offer(Keys.EXPERIENCE_SINCE_LEVEL, newxp<maxxp?newxp:maxxp-1);
	}
	
	public static void loadConfig(ConfigurationNode node) throws ObjectMappingException {
		mode = XPpipe.Mode.valueOf(node.getNode("XPHandling").getString("IGNORE"));
		globalMultiplier = node.getNode("XPmultiplierGlobal").getDouble(1.0);
		xpMultipliers.clear();
		node.getNode("XPmultiplier").getChildrenMap().forEach((k,v)->{
			Optional<EntityType> type = Sponge.getRegistry().getType(EntityType.class, (String)k);
			if (!type.isPresent()) WarCraft.l("No such entityType: %s",(String)k);
			else {
				Double value = v.getDouble(1.0);
				if (value < 0.0) value = 0.0;
				xpMultipliers.put(type.get(), value);
				WarCraft.l("XP multiplier for %s: %f", type.get().getId(), value);
			}
		});
	}
}
