package de.dosmike.sponge.WarCraftMC;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import de.dosmike.sponge.VersionChecker;
import de.dosmike.sponge.WarCraftMC.Manager.PermissionRegistry;
import de.dosmike.sponge.WarCraftMC.Manager.PlayerStateManager;
import de.dosmike.sponge.WarCraftMC.Manager.RaceManager;
import de.dosmike.sponge.WarCraftMC.Manager.SkillManager;
import de.dosmike.sponge.WarCraftMC.commands.CommandRegister;
import de.dosmike.sponge.WarCraftMC.data.DataKeys;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ImmutableProjectileWeaponData;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.ProjectileWeaponData;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.impl.ImmutableProjectileWeaponDataImpl;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.impl.ProjectileWeaponBuilder;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.impl.ProjectileWeaponDataImpl;
import de.dosmike.sponge.langswitch.LocalizedText;
import de.dosmike.sponge.languageservice.API.LanguageService;
import de.dosmike.sponge.languageservice.API.Localized;
import de.dosmike.sponge.languageservice.API.PluginTranslation;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(id = "dosmike_warcraft", name = "WarCraft MC", version = "0.5.6")
public class WarCraft {

	//default vars...
	@Inject
	private PluginContainer container;

	static WarCraft instance;
	public static PluginTranslation T() { return translator; }
	//public static LanguageService LS() { return languageService; }

	private static PluginTranslation translator = null;
	//private static LanguageService languageService = null;

	private static PermissionService permissions = null;
	public static Optional<PermissionService> getPermissions() { return Optional.ofNullable(permissions); }
	public static Optional<PermissionDescription.Builder> describePermission() {
		return getPermissions().map(p->p.newDescriptionBuilder(instance));
	}

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
	public static void l(String format, Object... args) { instance.logger.info(String.format(format, args)); }	public static void w(String format, Object... args) { instance.logger.warn(String.format(format, args)); }
	public static void tell(Object... message) {
		Sponge.getServer().getBroadcastChannel().getMembers().forEach(receiver->{
			if (receiver instanceof Player) {
				tell(receiver, message);
			} else {
				Text.Builder tb = Text.builder();
				tb.color(TextColors.GOLD);
				tb.append(Text.of("[WC] "));
				if (!(message[0] instanceof TextColor)) tb.color(TextColors.RESET);
				for (Object o : message) {
					if (o instanceof TextColor)
						tb.color((TextColor)o);
					else if (o instanceof LocalizedText) {
						tb.append(((LocalizedText) o).setContextColor(tb.getColor()).orLiteral(Sponge.getServer().getConsole()));
					}
					else if (o instanceof Localized) {
						Object local = ((Localized)o).orLiteral(Sponge.getServer().getConsole());
						if (local instanceof Text) {
							tb.append((Text)local);
						} else {
							tb.append(Text.of(o));
						}
					}
					else
						tb.append(Text.of(o));
				}
			}
		});
	}
	public static void tell(Player target, Object... message) {
		Text.Builder tb = Text.builder();
		tb.color(TextColors.GOLD);
		tb.append(Text.of("[WC] "));
		if (!(message[0] instanceof TextColor)) tb.append(Text.of(TextColors.RESET));
		for (Object o : message) {
			if (o instanceof TextColor) 
				tb.append(Text.of((TextColor)o));
			else if (o instanceof LocalizedText) {
				tb.append(((LocalizedText) o).setContextColor(tb.getColor()).orLiteral(target));
			}
			else if (o instanceof Localized) {
				Object local = ((Localized) o).orLiteral(target);
				if (local instanceof Text) {
					tb.append((Text)local);
				} else {
					tb.append(Text.of(o));
				}
			}
			else
				tb.append(Text.of(o));
		}
		target.sendMessage(tb.build());
	}

	//region Custom Data

    private DataRegistration<ProjectileWeaponData, ImmutableProjectileWeaponData> PROJECTILE_WEAPON_DATA_REGISTRATION;
	@Listener
	public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> event) {
	    DataKeys.PROJECTILE_WEAPON = Key.builder()
                .type(new TypeToken<Value<ItemStackSnapshot>>(){})
                .id("warcraft:projectileweapon")
                .name("Projectile Weapon")
                .query(DataQuery.of("Weapon"))
                .build();
	}

	@Listener
	public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?,?>> event) {
	    final DataManager manager = Sponge.getDataManager();
	    //ItemStackSnapshot should already have a builder
        PROJECTILE_WEAPON_DATA_REGISTRATION = DataRegistration.builder()
                .dataClass(ProjectileWeaponData.class)
                .dataImplementation(ProjectileWeaponDataImpl.class)
                .immutableClass(ImmutableProjectileWeaponData.class)
                .immutableImplementation(ImmutableProjectileWeaponDataImpl.class)
                .builder(new ProjectileWeaponBuilder())
                .name("Projectile Weapon Data")
                .id("warcraft:weapon")
                .build();
	}

	//endregion


	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(LanguageService.class)) {
			LanguageService languageService = (LanguageService) event.getNewProvider();
			translator = languageService.registerTranslation(this); //add this plugin to langswitch
		} else if (event.getService().equals(PermissionService.class)) {
			permissions = (PermissionService)event.getNewProvider();
		}
	}
	
	@Listener
	public void onServerInit(GameInitializationEvent event) {
		instance = this;
//		L = LangSwitch.createTranslation(this);
		configDir.toFile().mkdirs();
		raceConfigManager = HoconConfigurationLoader.builder().setPath(configDir.resolve("races.conf")).build();

		checkDefaultConfigs();
		loadConfig();
		
//		EventCause.WarCraftBaseCause = Cause.of(NamedCause.of("WarCraft", Sponge.getPluginManager().fromInstance(this).get()));
		
		Sponge.getEventManager().registerListeners(this, new SpongeEventListeners());
		Sponge.getEventManager().registerListeners(this, new WarCraftEventListeners());
	}

	private void checkDefaultConfigs() {
		Path general_config = getConfigDir().getParent().resolve("dosmike_warcraft.conf");
		Path races_config = getConfigDir().resolve("races.conf");
		Path english_translation = getConfigDir().resolve("Lang").resolve("en.lang");

		if (!english_translation.toFile().exists()) {
			l("Extracting default translation");
			english_translation.toFile().getParentFile().mkdirs();
			copyAssetStream("config/en.lang", english_translation.toFile(), "  --  Unable to write default translation --");
		}
		if (!general_config.toFile().exists()) {
			w("General Config is missing, extracting default!");
			copyAssetStream("config/dosmike_warcraft.conf", general_config.toFile(), " -- Unable to write default config -- ");
		} else {
			l("General Config found");
		}
		if (!races_config.toFile().exists()) {
			w("Race Config is missing, extracting default!");
			copyAssetStream("config/races.conf", races_config.toFile(), " -- Unable to write default config -- ");
		} else {
			l("Race Config found");
		}
	}
	private static void copyAssetStream(String resourceName, File output, String errWarn) {
		InputStream in = null;
		BufferedOutputStream bos = null;
		try {
			in = WarCraft.class.getClassLoader().getResourceAsStream(resourceName);
			bos = new BufferedOutputStream(new FileOutputStream(output));
			byte[] buffer = new byte[512]; int r;
			while ((r = in.read(buffer,0,buffer.length))>0) {
				bos.write(buffer, 0, r);
			}
			bos.flush();
		} catch (IOException e) {
			w(errWarn);
			e.printStackTrace();
		} finally {
			try { in.close(); } catch (Exception ignore) {}
			try { bos.close(); } catch (Exception ignore) {}
		}
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		CommandRegister.RegisterCommands(this);
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				long timer = System.currentTimeMillis();
				
				SkillManager.nadeTick();
				
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

		VersionChecker.conformAuto(this, configDir, "versionchecker.conf");

		l("Please wait, loading races...");
		new RaceManager(this);
		RaceManager.loadRaces();
		l("%d races loaded from disk", RaceManager.getRaces().size());
	}
	
	@Listener
	public void onServerStopping(GameStoppingEvent event) {
		//prevent concurrent modification exceptions when unloading
		Set<Profile> concurrentCopy = new HashSet<>(Profile.profileCache.values());
		concurrentCopy.forEach(Profile::saveAndUnload);
	}
	
	
	@Listener
	public void onReload(GameReloadEvent event) {
		loadConfig();
		Sponge.getServer().getConsole().sendMessage(WarCraft.T().localText("configreload").orLiteral(Sponge.getServer().getConsole()));
	}
	
	static List<String> inactiveWorlds = new LinkedList<>();
//	static String activePermission=null;
	
	@SuppressWarnings("serial")
	private void loadConfig() {
		try {
			CommentedConfigurationNode root = configManager.load();
			inactiveWorlds = root.getNode("WarCraftWorldBL").getValue(new TypeToken<List<String>>(){}, new LinkedList<String>());
//			activePermission = root.getNode("WarCraftPermission").getString();
			String permString = root.getNode("WarCraftPermission").getString();
			if (permString != null) {
				PermissionRegistry.register("active", permString, Text.of("This permission is required to be considered playing WarCraft"), PermissionDescription.ROLE_USER);
			} else {
				PermissionRegistry.unregister("active");
			}
			
			XPpipe.loadConfig(root);
			WarCraft.l("Config: set XP handling to " + XPpipe.getMode().toString());
			
			ManaPipe.loadConfig(root);
			WarCraft.l("Config: set mana source to "+ ManaPipe.mode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
