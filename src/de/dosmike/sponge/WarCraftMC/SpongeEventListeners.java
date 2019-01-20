package de.dosmike.sponge.WarCraftMC;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent.Death;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import de.dosmike.sponge.WarCraftMC.Manager.DamageManager;
import de.dosmike.sponge.WarCraftMC.Manager.NextSpawnActionManager;
import de.dosmike.sponge.WarCraftMC.Manager.PlayerStateManager;
import de.dosmike.sponge.WarCraftMC.Manager.SkillManager;
import de.dosmike.sponge.WarCraftMC.catalogs.ResultProperty;
import de.dosmike.sponge.WarCraftMC.catalogs.SkillResult;
import de.dosmike.sponge.WarCraftMC.effects.wceRootLiving;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import de.dosmike.sponge.mikestoolbox.event.BoxCombatEvent;
import de.dosmike.sponge.mikestoolbox.event.BoxJumpEvent;
import de.dosmike.sponge.mikestoolbox.event.BoxSneakEvent;
import de.dosmike.sponge.mikestoolbox.event.BoxSprintEvent;

/** This will handle all events from players interacting with the worlds and other entities */
public class SpongeEventListeners {

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
//		WarCraft.l("Loading profile for " + event.getTargetEntity().getName());
		Profile p = Profile.loadOrCreate(event.getTargetEntity());
		if (p.getRaceData().isPresent())
			WarCraft.tell(event.getTargetEntity(), "Wellcom back, ", TextColors.GOLD, event.getTargetEntity().getName(), Text.builder(" of the ").color(TextColors.RESET).build(), p.getRaceData().get().getRace().getName());
		
		restoreKeyedDefaults(event.getTargetEntity());
	}
	
	@Listener
	public void onPlayerPart(ClientConnectionEvent.Disconnect event) {
//		BoxLiving.removeCustomEffect(event.getTargetEntity(), CustomEffect.class); //remove all effects, now handled by mtb
		NextSpawnActionManager.removeAll(event.getTargetEntity());
		PlayerStateManager.forceOut(event.getTargetEntity());
		Profile.loadOrCreate(event.getTargetEntity()).saveAndUnload();
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
			Profile profile = Profile.loadOrCreate((Player)event.getTargetEntity());
			ActionData data = ActionData.builder(Trigger.ONJUMP)
					.setSelf((Player)event.getTargetEntity())
					.build();
//			Optional<SkillResult> result =
					profile.getRaceData().ifPresent(race->race.fire(profile, data));
//			if (result.isPresent()) {
//				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
//			}
		}
	}
	
	@Listener
	public void onCombatEntity(BoxCombatEvent event) {
		Living attacker = event.getSourceEntity();
		Living target = event.getTargetEntity();
		if (attacker.equals(target)) return; //we won't give xp for being stupid and hurting yourself
		if (wceRootLiving.frozenEntities.contains(attacker.getUniqueId())) event.setCancelled(true);
		double targetdamage = event.getOriginal().getFinalDamage();
		double targethealth = target.get(Keys.HEALTH).orElse(0.0);
		if (targetdamage > targethealth) targetdamage = targethealth; //we won't give you more xp for killing a bat just because you used your op sword

		//do something with the data:
		//fx stuff
		if (attacker instanceof Player && !event.getOriginal().willCauseDeath()) { //if even willCauseDeath this would just waste mana
			Profile profile = Profile.loadOrCreate((Player)attacker);
			ActionData data = ActionData.builder(Trigger.ONATTACK)
					.setSelf((Player)attacker)
					.setOpponent(target)
					.setDamage(targetdamage)
					.build();
			Optional<SkillResult> result = profile.getRaceData().flatMap(race->race.fire(profile, data));
			if (result.isPresent()) {
				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
				else {
					Double modifier = 0.0;
					for (Double mod : result.get().get(ResultProperty.MODIFY_DAMAGE)) modifier+=mod;
					event.getOriginal().setBaseDamage(event.getOriginal().getBaseDamage()+modifier);
				}
			}
		}
		if (target instanceof Player) {
			Profile profile = Profile.loadOrCreate((Player)target);
			ActionData data = ActionData.builder(Trigger.ONHIT)
					.setSelf((Player)target)
					.setOpponent((Living)attacker)
					.setDamage(targetdamage)
					.build();
			Optional<SkillResult> result = profile.getRaceData().flatMap(race->race.fire(profile, data));
			if (result.isPresent()) {
				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
			}
		}
		if (event.getOriginal().willCauseDeath() && target instanceof Player) {
			Profile profile = Profile.loadOrCreate((Player)target);
			ActionData data = ActionData.builder(Trigger.ONDEATH)
					.setSelf((Player)target)
					.setOpponent((Living)attacker)
					.setDamage(targetdamage)
					.build();
			profile.getRaceData().ifPresent(race->race.fire(profile, data));
			Optional<SkillResult> result = profile.getRaceData().flatMap(race->race.fire(profile, data));
			if (result.isPresent()) {
				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
			}
		}
		
		//xp stuff
		if (attacker instanceof Player) {
			DamageManager.damage((Player)attacker, target, targetdamage);
		}
	}
	
//	@Listener
//	public void onAttackEntity(DamageEntityEvent event) {
//		//prepare interesting event data: source, target, damage
//		
//		if (event.isCancelled()) return;
//		if (!(event.getTargetEntity() instanceof Living)) return;
//		Living target = (Living)event.getTargetEntity();
//		Optional<EntityDamageSource> source = event.getCause().first(EntityDamageSource.class);
//		if (!source.isPresent()) return;
//		Entity attacker = source.get().getSource();
//		if (!(attacker instanceof Living)) { //resolve indirect damage source
//			if (!attacker.getCreator().isPresent()) return;
//			Optional<Entity> perhaps = target.getWorld().getEntity(attacker.getCreator().get()); //Sponge.getServer().getPlayer(attacker.getCreator().get());
//			if (!perhaps.isPresent() || !(perhaps.get() instanceof Living)) return;
//			attacker = perhaps.get();
//		}
//		if (attacker.equals(target)) return; //we won't give xp for being stupid and hurting yourself
//		if (wceRootLiving.frozenEntities.contains(attacker.getUniqueId())) event.setCancelled(true);
//		double targetdamage = event.getFinalDamage();
//		double targethealth = target.get(Keys.HEALTH).orElse(0.0);
//		if (targetdamage > targethealth) targetdamage = targethealth; //we won't give you more xp for killing a bat just because you used your op sword
//
//		//do something with the data:
//		//fx stuff
//		if (attacker instanceof Player && !event.willCauseDeath()) { //if even willCauseDeath this would just waste mana
//			Profile profile = Profile.loadOrCreate((Player)attacker);
//			ActionData data = ActionData.builder(Trigger.ONATTACK)
//					.setSelf((Player)attacker)
//					.setOpponent(target)
//					.setDamage(targetdamage)
//					.build();
//			Optional<SkillResult> result = profile.getRaceData().get().fire(profile, data);
//			if (result.isPresent()) {
//				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
//				else {
//					Double modifier = 0.0;
//					for (Double mod : result.get().get(ResultProperty.MODIFY_DAMAGE)) modifier+=mod;
//					event.setBaseDamage(event.getBaseDamage()+modifier);
//				}
//			}
//		}
//		if (target instanceof Player) {
//			Profile profile = Profile.loadOrCreate((Player)target);
//			ActionData data = ActionData.builder(Trigger.ONHIT)
//					.setSelf((Player)target)
//					.setOpponent((Living)attacker)
//					.setDamage(targetdamage)
//					.build();
//			Optional<SkillResult> result = profile.getRaceData().get().fire(profile, data);
//			if (result.isPresent()) {
//				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
//			}
//		}
//		if (event.willCauseDeath() && target instanceof Player) {
//			Profile profile = Profile.loadOrCreate((Player)target);
//			ActionData data = ActionData.builder(Trigger.ONDEATH)
//					.setSelf((Player)target)
//					.setOpponent((Living)attacker)
//					.setDamage(targetdamage)
//					.build();
//			profile.getRaceData().get().fire(profile, data);
//			Optional<SkillResult> result = profile.getRaceData().get().fire(profile, data);
//			if (result.isPresent()) {
//				if (result.get().get(ResultProperty.CANCEL_ACTION).contains(true)) event.setCancelled(true);
//			}
//		}
//		
//		//xp stuff
//		if (attacker instanceof Player) {
//			DamageManager.damage((Player)attacker, target, targetdamage);
//		}
//	}
	
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
//		Optional<Player> source = event.getCause().first(Player.class);
//		EventCause cause = source.isPresent()?new EventCause(source.get()):new EventCause();
//		cause.bake(NamedCause.hitTarget(target)); //bake the hit target ontop of the cause
		
//		BoxLiving.removeCustomEffect(event.getTargetEntity(), CustomEffect.class); //remove all effects - now handeled by mtb
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
	
//	@Listener
//	public void onEntityXPChange(ChangeEntityExperienceEvent event) {
//		if (!(event.getTargetEntity() instanceof Player)) return;
//		WarCraft.l(event.getTargetEntity() + " Changed XP " + event.getOriginalExperience() + " - " + event.getExperience());
//		if (XPpipe.processVanillaXP((Player) event.getTargetEntity(), Profile.loadOrCreate((Player)event.getTargetEntity()), event.getExperience()-event.getOriginalExperience(), event.getCause()))
//			event.setCancelled(true);
//	}
	
//	@SuppressWarnings("unchecked")
	@Listener
	public void onColideEntity(CollideEntityEvent event) {
		Optional<Player> target = event.getCause().first(Player.class);
		if (!target.isPresent()) return;
		
		////nade collision... kinda
		for (Entity e : event.getEntities()) if (SkillManager.nades.containsKey(e)) {
			event.setCancelled(true);
		}
		
		//can't figure xp orbs... so let's just spam that function, it gets the xp... somehow ;D
		XPpipe.processVanillaXP(target.get(), Profile.loadOrCreate(target.get()));
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
			Profile profile = Profile.loadOrCreate((Player)player.get());
			ActionData data = ActionData.builder(Trigger.ONBLOCK)
					.setSelf((Player)player.get())
					.setItem(event.getItemStack().createStack())
					.build();
			profile.getRaceData().ifPresent(race->race.fire(profile, data));
		}
	}

	@Listener
	public void onStartSprinting(BoxSprintEvent event) {
		Player player = event.getTargetEntity();
		UUID playerID = player.getUniqueId();
		
		Optional<Profile> profile = Profile.getIfOnline(playerID);
		if (!profile.isPresent()) return;
		ActionData actiondata = ActionData.builder(Trigger.ONSPRINT)
				.setSelf(player)
				.build();
		profile.get().getRaceData().ifPresent(race->race.fire(profile.get(), actiondata));
	}
	
	@Listener
	public void onStartSneaking(BoxSneakEvent event) {
		Player player = event.getTargetEntity();
		UUID playerID = player.getUniqueId();
		
		Optional<Profile> profile = Profile.getIfOnline(playerID);
		if (!profile.isPresent()) return;
		ActionData actiondata = ActionData.builder(Trigger.ONSNEAK)
				.setSelf(player)
				.build();
		profile.get().getRaceData().ifPresent(race->race.fire(profile.get(), actiondata));
	}
	
}
