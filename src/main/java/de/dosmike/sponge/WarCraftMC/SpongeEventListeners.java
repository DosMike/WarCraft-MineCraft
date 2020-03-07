package de.dosmike.sponge.WarCraftMC;

import de.dosmike.sponge.WarCraftMC.Manager.DamageManager;
import de.dosmike.sponge.WarCraftMC.Manager.NextSpawnActionManager;
import de.dosmike.sponge.WarCraftMC.Manager.PlayerStateManager;
import de.dosmike.sponge.WarCraftMC.Manager.SkillManager;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.data.DataKeys;
import de.dosmike.sponge.WarCraftMC.data.ProjectileWeapon.impl.ProjectileWeaponDataImpl;
import de.dosmike.sponge.WarCraftMC.effects.wceRootLiving;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import de.dosmike.sponge.mikestoolbox.event.BoxCombatEvent;
import de.dosmike.sponge.mikestoolbox.event.BoxJumpEvent;
import de.dosmike.sponge.mikestoolbox.event.BoxSneakEvent;
import de.dosmike.sponge.mikestoolbox.event.BoxSprintEvent;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.BlockProjectileSource;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent.Death;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

/** This will handle all events from players interacting with the worlds and other entities */
public class SpongeEventListeners {

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		Profile p = Profile.loadOrCreate(event.getTargetEntity());
		if (Profile.isActive(event.getTargetEntity(), p) && p.getRaceData().isPresent())
			WarCraft.tell(event.getTargetEntity(), WarCraft.T().localText("player.wellcome")
					.replace("$name", event.getTargetEntity().getName())
					.replace("$race", p.getRaceData().get().getRace().getName())
					);
		
		restoreKeyedDefaults(event.getTargetEntity());
	}
	
	@Listener
	public void onPlayerPart(ClientConnectionEvent.Disconnect event) {
		NextSpawnActionManager.removeAll(event.getTargetEntity());
		PlayerStateManager.forceOut(event.getTargetEntity());
		Profile.getIfOnline(event.getTargetEntity().getUniqueId())
				.ifPresent(Profile::saveAndUnload);
		ManaPipe.dropPlayer(event.getTargetEntity());
	}
	
	//restore manipulated player data, currently there's not other good way
	public static void restoreKeyedDefaults(Player player) {
		player.offer(Keys.WALKING_SPEED, 0.1); //for bukkit default was 0.2, for sponge default seems to be 0.1
		player.offer(Keys.MAX_HEALTH, 20.0);
	}
	
	@Listener
	public void onPlayerJump(BoxJumpEvent event) {
		if (event.getTargetEntity() instanceof Player) {
			Profile.getIfActive((Player) event.getTargetEntity()).ifPresent(profile-> {
				ActionData data = ActionData.builder(Trigger.ONJUMP)
							.setSelf((Player) event.getTargetEntity())
							.build();
				profile.getRaceData().get().fire(profile, data);
			});
		}
	}

	/** attach weapon information to the projectile for the DamageEntityEvent to read back
	 * Seems to not be called for player */
//	@Listener
//	public void onShootArrow(LaunchProjectileEvent event) {
//		...
//	}

	/** attach weapon information to the projectile for the DamageEntityEvent to read back */
	@Listener
	public void onShootArrow(ConstructEntityEvent.Post event) {
		if (!(event.getTargetEntity() instanceof Projectile)) return;
		Projectile projectile = (Projectile)event.getTargetEntity();
		Optional<ItemStackSnapshot> item = event.getContext().get(EventContextKeys.USED_ITEM);
//		WarCraft.l("Trying to attaching projectile data via ConstructEntityEvent.Post");
//		item.ifPresent(itemStackSnapshot -> new ProjectileWeapon(itemStackSnapshot).attachTo(projectile));
		item.filter(weapon->!weapon.getType().equals(ItemTypes.AIR))
				.map(weapon->projectile.offer(new ProjectileWeaponDataImpl(weapon)))
				.filter(result->!result.getRejectedData().isEmpty()).ifPresent(result->{
			ProjectileSource ps = projectile.getShooter();
			String projectileSource = "UNKNOWN SOURCE";
			if (ps instanceof BlockProjectileSource) {
				Location<World> location = ((BlockProjectileSource) ps).getLocation();
				projectileSource = ((BlockProjectileSource) ps).getBlock().getType().getId()+" @ "+
						location.getExtent().getUniqueId().toString()+"/"+
						location.getBlockX()+"/"+location.getBlockY()+"/"+location.getBlockZ();
			} else if (ps instanceof Entity) {
				projectileSource = ((Entity) ps).getType().getId() + " (" + ((Entity) ps).getUniqueId().toString()+")";
			}
			WarCraft.w("Could not attach weapon data to projectile [Construct Entity Event : Projectile %s, Source %s, Weapon %s]", projectile.getType().getId(), projectileSource, item.get().getType().getId());
		});
	}

	@Listener
	public void onCombatEntity(BoxCombatEvent event) {
		Living attacker = event.getSourceEntity();
		Living target = event.getTargetEntity();
		Optional<ItemStackSnapshot> weapon = event.getOriginal().getContext().get(EventContextKeys.USED_ITEM);
		//Try to read custom weapon data in case of arrow
		if (!weapon.isPresent()) {
			//get projectile
			Optional<IndirectEntityDamageSource> damageSource = event.getOriginal().getCause()
					.allOf(IndirectEntityDamageSource.class).stream()
					.filter(source -> source.getType().equals(DamageTypes.PROJECTILE)).findFirst();
			if (damageSource.isPresent()) {
				IndirectEntityDamageSource source = damageSource.get();
				Entity projectile = source.getSource();
				weapon = projectile.get(DataKeys.PROJECTILE_WEAPON);
//				weapon = ProjectileWeapon.readFrom(projectile).map(ProjectileWeapon::getWeapon);
			}
		}
//		WarCraft.l("Weapon: %s", weapon.orElse(ItemStackSnapshot.NONE).getType().getId());
		if (attacker.equals(target)) return; //we won't give xp for being stupid and hurting yourself
		if (wceRootLiving.frozenEntities.contains(attacker.getUniqueId())) event.setCancelled(true);
		double targetdamage = event.getOriginal().getFinalDamage();
		double targethealth = target.get(Keys.HEALTH).orElse(0.0);
		if (targetdamage > targethealth) targetdamage = targethealth; //we won't give you more xp for killing a bat just because you used your op sword

		//do something with the data:
		//fx stuff
		if (attacker instanceof Player && !event.getOriginal().willCauseDeath()) { //if event willCauseDeath this would just waste mana
			Optional<Profile> profile = Profile.getIfActive((Player)attacker);
			ActionData data = ActionData.builder(Trigger.ONATTACK)
					.setSelf((Player)attacker)
					.setOpponent(target)
					.setItem(weapon.orElse(ItemStackSnapshot.NONE).createStack())
					.setDamage(targetdamage)
					.build();
			Optional<SkillResult> result = profile.flatMap(p->p.getRaceData().flatMap(race->race.fire(p, data)));
			if (result.isPresent()) {
				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) {
					event.setCancelled(true);
				} else {
					Double modifier = 0.0;
					for (Double mod : result.get().get(ResultProperty.MODIFY_DAMAGE))
						modifier+=mod;
					event.getOriginal().setBaseDamage(event.getOriginal().getBaseDamage()+modifier);
				}
			}
		}
		if (target instanceof Player) {
			Optional<Profile> profile = Profile.getIfActive((Player)target);
			ActionData data = ActionData.builder(Trigger.ONHIT)
					.setSelf((Player)target)
					.setOpponent((Living)attacker)
					.setItem(weapon.orElse(ItemStackSnapshot.NONE).createStack())
					.setDamage(targetdamage)
					.build();
			Optional<SkillResult> result = profile.flatMap(p->p.getRaceData().flatMap(race->race.fire(p, data)));
			if (result.isPresent()) {
				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) {
					event.setCancelled(true);
				}
			}
		}
		if (event.getOriginal().willCauseDeath() && target instanceof Player) {
			Optional<Profile> profile = Profile.getIfActive((Player)target);
			ActionData data = ActionData.builder(Trigger.ONDEATH)
					.setSelf((Player)target)
					.setOpponent((Living)attacker)
					.setItem(weapon.orElse(ItemStackSnapshot.NONE).createStack())
					.setDamage(targetdamage)
					.build();
//			profile.getRaceData().ifPresent(race->race.fire(profile, data)); //why was this called another time here?
			Optional<SkillResult> result = profile.flatMap(p->p.getRaceData().flatMap(race->race.fire(p, data)));
			if (result.isPresent()) {
				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
			}
		}
		
		//xp stuff
		if (attacker instanceof Player) {
			DamageManager.damage((Player)attacker, target, targetdamage);
		}
	}
	
	@Listener
	public void onLaunchProjectile(LaunchProjectileEvent event) {
		Optional<Living> shooter = event.getCause().first(Living.class);
		if (!shooter.isPresent()) return;
		if (wceRootLiving.frozenEntities.contains(shooter.get()))
			event.setCancelled(true);
	}
	
	@Listener
	public void onDeath(Death event) {
		Living target = event.getTargetEntity();

		DamageManager.death(target.getUniqueId(), XPpipe.getMultiplierOr1(target.getType()));
	}
	
	@Listener
	public void onRespawn(RespawnPlayerEvent event) {
		//use getIfOnline because the original player is probably still dead or in another dimension. or dead
		NextSpawnActionManager.onSpawn(event.getTargetEntity());
		
		Optional<Profile> profile = Profile.getIfOnline(event.getOriginalPlayer().getUniqueId());
		if (!profile.isPresent()) return;
		ActionData data = ActionData.builder(Trigger.ONSPAWN)
				.setSelf(event.getTargetEntity())
				.build();
		profile.get().getRaceData().ifPresent(race->race.fire(profile.get(), data));
		ManaPipe.resetMana(event.getTargetEntity());
	}

	@Listener
	public void onColideEntity(CollideEntityEvent event) {
		Optional<Player> target = event.getCause().first(Player.class);
		if (!target.isPresent()) return;
		
		////nade collision... kinda
		for (Entity e : event.getEntities()) if (SkillManager.nades.containsKey(e)) {
			event.setCancelled(true);
		}
		
		//can't figure xp orbs... so let's just spam that function, it gets the xp... somehow ;D
		Profile.getIfActive(target.get()).ifPresent(prof->
			XPpipe.processVanillaXP(target.get(), prof)
		);
	}
	
	@Listener
	public void onConsumeItem(UseItemStackEvent.Finish event) {
		Optional<Player> target = event.getCause().first(Player.class);
		if (!target.isPresent()) return;
		ManaPipe.consumedManaItem(target.get(), event.getItemStackInUse());
	}
	
	/** <b>Be very carefull with this event - Can get called up to 50 times per second per entity!</b> */
	@Listener
	public void onMove(MoveEntityEvent event) {
		if ((event.getTargetEntity() instanceof Living) && wceRootLiving.frozenEntities.contains(event.getTargetEntity().getUniqueId())) 
			{ event.setCancelled(true); return; }
	}
	
	@Listener
	public void onBlockShield(InteractItemEvent event) {
		Optional<Player> player = event.getCause().first(Player.class);
		if (!player.isPresent()) return;
		if (event.getItemStack().getType().equals(ItemTypes.SHIELD)) {
			Profile.getIfActive(player.get()).ifPresent(profile -> {
				ActionData data = ActionData.builder(Trigger.ONBLOCK)
						.setSelf(player.get())
						.setItem(event.getItemStack().createStack())
						.build();
				profile.getRaceData().get().fire(profile, data);
			});
		}
	}

	@Listener
	public void onStartSprinting(BoxSprintEvent event) {
		Player player = event.getTargetEntity();
		UUID playerID = player.getUniqueId();
		
		Profile.getIfActive(player).ifPresent(profile->{
			ActionData actiondata = ActionData.builder(Trigger.ONSPRINT)
					.setSelf(player)
					.build();
			profile.getRaceData().get().fire(profile, actiondata);
		});
	}
	
	@Listener
	public void onStartSneaking(BoxSneakEvent event) {
		Player player = event.getTargetEntity();
		UUID playerID = player.getUniqueId();
		
		Profile.getIfActive(player).ifPresent(profile->{
			ActionData actiondata = ActionData.builder(Trigger.ONSNEAK)
					.setSelf(player)
					.build();
			profile.getRaceData().get().fire(profile, actiondata);
		});
	}
	
}
