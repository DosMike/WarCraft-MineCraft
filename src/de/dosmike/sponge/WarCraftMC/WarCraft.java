package de.dosmike.sponge.WarCraftMC;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import de.dosmike.sponge.WarCraftMC.Manager.PlayerStateManager;
import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.Manager.StatusEffectManager;
import de.dosmike.sponge.WarCraftMC.commands.CommandRegister;
import de.dosmike.sponge.WarCraftMC.events.EventCause;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "dosmike_warcraft", name = "WarCraft MC", version = "0.1")
public class WarCraft {

	//default vars...
	static WarCraft instance;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	Path getConfigDir() { return configDir; }
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	private ConfigurationLoader<CommentedConfigurationNode> raceConfigManager;
	public ConfigurationLoader<CommentedConfigurationNode> getRaceConfig() {
		return raceConfigManager;
	}
	
	@Inject
	private Logger logger;
	public static void l(String format, Object... args) { instance.logger.info(String.format(format, args)); }
	public static void w(String format, Object... args) { instance.logger.warn(String.format(format, args)); }
	public static void tell(Object... message) {
		Text.Builder tb = Text.builder();
		tb.color(TextColors.GOLD);
		tb.append(Text.of("[WC] "));
		if (!(message[0] instanceof TextColor)) tb.color(TextColors.RESET);
		for (Object o : message) {
			if (o instanceof TextColor) 
				tb.color((TextColor)o);
			else
				tb.append(Text.of(o));
		}
		Sponge.getServer().getBroadcastChannel().send(tb.build());
	}
	public static void tell(Player target, Object... message) {
		Text.Builder tb = Text.builder();
		tb.color(TextColors.GOLD);
		tb.append(Text.of("[WC] "));
		if (!(message[0] instanceof TextColor)) tb.append(Text.of(TextColors.RESET));
		for (Object o : message) {
			if (o instanceof TextColor) 
				tb.append(Text.of((TextColor)o));
			else
				tb.append(Text.of(o));
		}
		target.sendMessage(tb.build());
	}
	
	@Listener
	public void onServerInit(GameInitializationEvent event) {
		instance = this;
		configDir.toFile().mkdirs();
		raceConfigManager = HoconConfigurationLoader.builder().setPath(configDir.resolve("races.conf")).build();
		
		loadConfig();
		
		EventCause.WarCraftBaseCause = Cause.of(NamedCause.of("WarCraft", this));
		
		Sponge.getEventManager().registerListeners(this, new SpongeEventListeners());
		Sponge.getEventManager().registerListeners(this, new WarCraftEventListeners());
		
		l("Please wait, loading races...");
		new RaceManager(this);
		RaceManager.loadRaces();
		l("%d races loaded from disk", RaceManager.getRaces().size());
	}
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		CommandRegister.RegisterCommands(this);
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				long timer = System.currentTimeMillis();
				
				StatusEffectManager.tick();
				
				timer = System.currentTimeMillis()-timer;
				if (timer>50) WarCraft.w("The plugin is running slow - Event timer took "+timer+"ms!");
			}
		}, 1000, 50, TimeUnit.MILLISECONDS);
		Sponge.getScheduler().createSyncExecutor(this).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				long timer = System.currentTimeMillis();
				
				PlayerStateManager.tick();
				ManaPipe.tick();
				
				timer = System.currentTimeMillis()-timer;
				if (timer>50) WarCraft.w("The plugin is running slow - State timer took "+timer+"ms!");
			}
		}, 1000, 500, TimeUnit.MILLISECONDS);
		
		PluginContainer container = Sponge.getPluginManager().fromInstance(this).get();
		tell(TextColors.GOLD, container.getName(), " [", container.getVersion().get(), "] is now ready!");
	}
	
	@Listener
	public void onServerStopping(GameStoppingEvent event) {
		//TODO save all palyers
	}
	
	@Listener
	public void onReload(GameReloadEvent event) {
		loadConfig();
		WarCraft.tell(TextColors.GREEN, "Config reloaded");
	}
	
	private void loadConfig() {
		try {
			CommentedConfigurationNode root = configManager.load();
			String mode = root.getNode("XPHandling").getString("IGNORE");
			XPpipe.mode = XPpipe.Mode.valueOf(mode);
			WarCraft.l("Config: set XP handling to " + mode);
			
			ManaPipe.loadConfig(root);
			WarCraft.l("Config: set mana source to "+ ManaPipe.mode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
