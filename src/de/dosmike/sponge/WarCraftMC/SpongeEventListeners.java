package de.dosmike.sponge.WarCraftMC;

import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
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
import de.dosmike.sponge.WarCraftMC.Manager.StatusEffectManager;
import de.dosmike.sponge.WarCraftMC.effects.wcEffect;
import de.dosmike.sponge.WarCraftMC.events.EventCause;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.WarCraftMC.races.ActionData;

/** This will handle all events from players interacting with the worlds and other entities */
public class SpongeEventListeners {

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		WarCraft.l("Loading profile for " + event.getTargetEntity().getName());
		Profile p = Profile.loadOrCreate(event.getTargetEntity());
		if (p.getRaceData().isPresent())
			WarCraft.tell(event.getTargetEntity(), "Wellcom back, ", TextColors.GOLD, event.getTargetEntity().getName(), Text.builder(" of the ").color(TextColors.RESET).build(), p.getRaceData().get().getRace().getName());
	}
	
	@Listener
	public void onPlayerPart(ClientConnectionEvent.Disconnect event) {
		NextSpawnActionManager.removeAll(event.getTargetEntity());
		StatusEffectManager.remove(event.getTargetEntity(), wcEffect.class); //remove all effects
		PlayerStateManager.forceOut(event.getTargetEntity());
		Profile.loadOrCreate(event.getTargetEntity()).saveAndUnload();
		ManaPipe.dropPlayer(event.getTargetEntity());
	}
	
	@Listener
	public void onAttackEntity(DamageEntityEvent event) {
		//prepare interesting event data: source, target, damage
		
		if (event.isCancelled()) return;
		if (!(event.getTargetEntity() instanceof Living)) return;
		Living target = (Living)event.getTargetEntity();
		Optional<EntityDamageSource> source = event.getCause().first(EntityDamageSource.class);
		if (!source.isPresent()) return;
		Entity attacker = source.get().getSource();
		if (!(attacker instanceof Living)) { //resolve indirect damage source
			if (!attacker.getCreator().isPresent()) return;
			Optional<Entity> perhaps = target.getWorld().getEntity(attacker.getCreator().get()); //Sponge.getServer().getPlayer(attacker.getCreator().get());
			if (!perhaps.isPresent() || !(perhaps.get() instanceof Living)) return;
			attacker = perhaps.get();
		}
		if (attacker.equals(target)) return; //we won't give xp for being stupid and hurting yourself
		if (StatusEffectManager.frozenEntities.contains(attacker.getUniqueId())) event.setCancelled(true);
		double targetdamage = event.getFinalDamage();
		double targethealth = target.get(Keys.HEALTH).orElse(0.0);
		if (targetdamage > targethealth) targetdamage = targethealth; //we won't give you more xp for killing a bat just because you used your op sword

		//do something with the data:
		//fx stuff
		if (attacker instanceof Player && !event.willCauseDeath()) { //if even willCauseDeath this would just waste mana
			Profile profile = Profile.loadOrCreate((Player)attacker);
			ActionData data = ActionData.builder(Trigger.ONATTACK)
					.setSelf((Player)attacker)
					.setOpponent(target)
					.setDamage(targetdamage)
					.build();
			profile.getRaceData().get().fire(profile, data);
		}
		if (target instanceof Player) {
			Profile profile = Profile.loadOrCreate((Player)target);
			ActionData data = ActionData.builder(Trigger.ONHIT)
					.setSelf((Player)target)
					.setOpponent((Living)attacker)
					.setDamage(targetdamage)
					.build();
			profile.getRaceData().get().fire(profile, data);
		}
		if (event.willCauseDeath() && target instanceof Player) {
			Profile profile = Profile.loadOrCreate((Player)target);
			ActionData data = ActionData.builder(Trigger.ONDEATH)
					.setSelf((Player)target)
					.setOpponent((Living)attacker)
					.setDamage(targetdamage)
					.build();
			profile.getRaceData().get().fire(profile, data);
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
		if (StatusEffectManager.frozenEntities.contains(shooter.get()))
			event.setCancelled(true);
	}
	
	@Listener
	public void onDeath(Death event) {
		Living target = event.getTargetEntity();
		Optional<Player> source = event.getCause().first(Player.class);
		EventCause cause = source.isPresent()?new EventCause(source.get()):new EventCause();
		cause.bake(NamedCause.hitTarget(target)); //bake the hit target ontop of the cause
		
		StatusEffectManager.remove(event.getTargetEntity(), wcEffect.class); //remove all effects
		DamageManager.death(target.getUniqueId(), 1.0, cause.get());
	}
	
	@Listener
	public void onRespawn(RespawnPlayerEvent event) {
		//use getIfOnline because the original player is probably still dead or in another dimension. or dead
		Optional<Profile> profile = Profile.getIfOnline(event.getOriginalPlayer().getUniqueId());
		if (!profile.isPresent()) return;
		ActionData data = ActionData.builder(Trigger.ONSPAWN)
				.setSelf(event.getTargetEntity())
				.build();
		profile.get().getRaceData().get().fire(profile.get(), data);
		ManaPipe.resetMana(event.getTargetEntity());
	}
	
//	@Listener
//	public void onEntityXPChange(ChangeEntityExperienceEvent event) {
//		if (!(event.getTargetEntity() instanceof Player)) return;
//		WarCraft.l(event.getTargetEntity() + " Changed XP " + event.getOriginalExperience() + " - " + event.getExperience());
//		if (XPpipe.processVanillaXP((Player) event.getTargetEntity(), Profile.loadOrCreate((Player)event.getTargetEntity()), event.getExperience()-event.getOriginalExperience(), event.getCause()))
//			event.setCancelled(true);
//	}
	
	@Listener
	public void onPickupItem(CollideEntityEvent event) {
		Optional<Player> target = event.getCause().first(Player.class);
		if (!target.isPresent()) return;
		
//		@SuppressWarnings("unchecked") //filtering for exp orbs, so they have to be
//		List<? extends Entity> entityItems = (List<? extends Entity>) event.filterEntities(entity -> entity.getType().equals(EntityTypes.EXPERIENCE_ORB));
//		int totalXP = 0;
//		for (Entity orb : entityItems) {
//			if (orb instanceof ExperienceOrb) totalXP++;
//		}
//		
//		WarCraft.l(target.get() + " collected " + totalXP + " Exp Orbs");
//		if (XPpipe.processVanillaXP(target.get(), Profile.loadOrCreate(target.get()), event.getCause()))
//			event.setCancelled(true);
		
		//can't figure xp orbs... so let's just spam that function, it'ss get the xp... somehow ;D
		XPpipe.processVanillaXP(target.get(), Profile.loadOrCreate(target.get()), event.getCause());
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
		if ((event.getTargetEntity() instanceof Living) && StatusEffectManager.frozenEntities.contains(event.getTargetEntity().getUniqueId())) 
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
			profile.getRaceData().get().fire(profile, data);
		}
	}
	
	@Listener
	public void dataChanged(ChangeDataHolderEvent.ValueChange event) {
		if (!(event.getTargetHolder() instanceof Player)) return;
		Player player = (Player)event.getTargetHolder();
		WarCraft.tell(player, "You did something!");
		
		for (ImmutableValue<?> data : event.getEndResult().getSuccessfulData()) {
			if (data.getKey().equals(Keys.IS_SPRINTING)) {
				Boolean sprinting = (Boolean) data.get();
				if (sprinting) WarCraft.tell(player, "You started sprinting");
			}
			if (data.getKey().equals(Keys.IS_SNEAKING)) {
				Boolean sneaking = (Boolean) data.get();
				if (sneaking) WarCraft.tell(player, "You started sneaking");
			}
		}
		
	}
}
