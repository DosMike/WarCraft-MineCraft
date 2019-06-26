package de.dosmike.sponge.WarCraftMC;

import java.util.*;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;

import com.dolhub.tech.MathEval;
import com.google.common.reflect.TypeToken;

import de.dosmike.sponge.WarCraftMC.effects.wceRestoreMana;
import de.dosmike.sponge.mikestoolbox.living.BoxLiving;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ManaPipe {
	public static enum Mode { FOODLEVEL, BOSSBAR } 
	
	static MathEval engine = new MathEval();
	static Random rng = new Random(System.currentTimeMillis());
	
	static ServerBossBar baseBar;
	
	static Mode mode;
	static String spawnMana; //formula
	static double regeneration; //mana over time
	static ItemType itemType;
	static int itemDV;
	static String itemRestore; //formula
	static String itemDuration; //formula
	
	static {
		baseBar = ServerBossBar.builder().color(BossBarColors.BLUE).createFog(false).darkenSky(false).name(Text.of("Mana")).overlay(BossBarOverlays.PROGRESS).playEndBossMusic(false).visible(true).build();
	}
	
	public static void loadConfig(ConfigurationNode node) throws ObjectMappingException {
		mode = Mode.valueOf(node.getNode("ManaHandling").getString("FOODLEVEL"));
		spawnMana = node.getNode("ManaSpawnAmount").getString("10*level").toLowerCase();
		regeneration = node.getNode("ManaRegeneration").getDouble(0.1);
		node = node.getNode("ManaRefill");
			itemType = node.getNode("ItemType").getValue(TypeToken.of(ItemType.class));
			if (itemType == null) return;
			itemDV = node.getNode("DataValue").getInt(0);
			itemRestore = node.getNode("Amount").getString("level+random/100").toLowerCase();
			itemDuration = node.getNode("Duration").getString("amount").toLowerCase();
	}
	
	public static double getMana(Player player) {
		if (mode==Mode.FOODLEVEL) {
			int val = player.get(Keys.FOOD_LEVEL).get();
//			WarCraft.l(player.getName()+" has FOOD_LEVEL "+val);
			return val;
		}
		if (!virtualMax.containsKey(player.getUniqueId())) resetMana(player);
		Double vm = virtualMana.get(player.getUniqueId());
		return vm==null?0:vm;
	}
	public static void setMana(Player player, double mana) {
		if (mode!=Mode.BOSSBAR) return;
		if (!virtualMax.containsKey(player.getUniqueId())) resetMana(player);
		int manamax = virtualMax.get(player.getUniqueId());
		if (mana>manamax)mana=manamax;
		virtualMana.put(player.getUniqueId(), mana);
		float perc = (float)mana/(float)manamax;
		ServerBossBar bar = manaBar.get(player.getUniqueId());
		bar.setPercent(perc);
	}
	public static void addMana(Player player, double amount) {
		if (mode!=Mode.BOSSBAR) return;
		if (!virtualMax.containsKey(player.getUniqueId())) resetMana(player);
		double mana = virtualMana.get(player.getUniqueId())+amount;
		int manamax = virtualMax.get(player.getUniqueId());
		if (mana>manamax)mana=manamax;
		virtualMana.put(player.getUniqueId(), mana);
		float perc = (float)mana/(float)manamax;
		ServerBossBar bar = manaBar.get(player.getUniqueId());
		bar.setPercent(perc);
	}
	public static void subMana(Player player, double amount) {
		if (mode!=Mode.BOSSBAR) return;
		//like with health and foodlevel i do not want this to drop while in creative - for more fun ;D
		GameMode gm = player.get(Keys.GAME_MODE).get();
		if (gm.equals(GameModes.CREATIVE) || gm.equals(GameModes.SPECTATOR)) return;
		if (!virtualMax.containsKey(player.getUniqueId())) resetMana(player);
		double mana = virtualMana.get(player.getUniqueId())-amount;
		int manamax = virtualMax.get(player.getUniqueId());
		if (mana<0)mana=0;
		virtualMana.put(player.getUniqueId(), mana);
		float perc = (float)mana/(float)manamax;
		ServerBossBar bar = manaBar.get(player.getUniqueId());
		bar.setPercent(perc);
	}

	/** completely refills the mana for this player */
	public static void resetMana(Player player) {
		if (mode!=Mode.BOSSBAR) return;
		Profile profile = Profile.loadOrCreate(player);
		if (!manaBar.containsKey(player.getUniqueId())) {
			ServerBossBar bar = ServerBossBar.builder().from(baseBar).percent(1f).build();
			bar.addPlayer(player);
			manaBar.put(player.getUniqueId(), bar);
		}
		if (profile.getRaceData().isPresent()) {
			try {
				virtualMax.put(player.getUniqueId(), 
						(int)engine.evaluate(
								spawnMana.replace("level", String.valueOf(profile.getRaceData().get().getLevel()))
								) );
			} catch (Exception e) {
				virtualMax.put(player.getUniqueId(), 10);
				WarCraft.w("Could not compute mana level!");
				e.printStackTrace();
			}
			virtualMana.put(player.getUniqueId(), (double)virtualMax.get(player.getUniqueId()));
		}
	}
	public static void dropPlayer(Player player) {
		if (mode!=Mode.BOSSBAR) return;
		if (manaBar.containsKey(player.getUniqueId())) manaBar.get(player.getUniqueId()).removePlayer(player);
		manaBar.remove(player.getUniqueId());
		virtualMax.remove(player.getUniqueId());
		virtualMana.remove(player.getUniqueId());
	}
	
	public static void tick() {
		if (mode!=Mode.BOSSBAR) return;
		Sponge.getServer().getOnlinePlayers().forEach(player -> {
			if (!Profile.isActive(player, null)) return;
			addMana(player, regeneration);
		});
	}
	
	public static void consumedManaItem(Player player, ItemStackSnapshot item) {
		if (mode!=Mode.BOSSBAR) return;
		if (itemType==null) return;
		if (!item.getType().equals(itemType)) return;
		DataQuery query = DataQuery.of('/', "UnsafeDamage");
		int dv = (int)item.toContainer().get(query).orElse(0);
		if (dv != itemDV) return;
//		WarCraft.l(player.getName()+"Consumed Mana Potion!");
		Optional<Profile> profile = Profile.getIfActive(player);
		if (!profile.isPresent()) return;
		int amount = (int)engine.evaluate(
				itemRestore.replace("level", String.valueOf(profile.get().getRaceData().get().getLevel()))
						.replace("random", String.valueOf(rng.nextFloat()*100)) );
		double duration = engine.evaluate(
				itemDuration.replace("amount", String.valueOf(amount)) );
		BoxLiving.addCustomEffect(player, new wceRestoreMana(
				duration, amount));
	}
	static Map<UUID, ServerBossBar> manaBar = new HashMap<>();
	static Map<UUID, Integer> virtualMax = new HashMap<>();
	static Map<UUID, Double> virtualMana = new HashMap<>();
}
