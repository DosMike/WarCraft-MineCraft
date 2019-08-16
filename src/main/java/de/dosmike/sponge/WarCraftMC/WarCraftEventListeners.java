package de.dosmike.sponge.WarCraftMC;

import de.dosmike.sponge.WarCraftMC.Manager.NextSpawnActionManager;
import de.dosmike.sponge.WarCraftMC.Manager.PlayerStateManager;
import de.dosmike.sponge.WarCraftMC.events.GainXPEvent;
import de.dosmike.sponge.WarCraftMC.events.LevelUpEvent;
import de.dosmike.sponge.WarCraftMC.events.ProfileStateChangeEvent;
import de.dosmike.sponge.WarCraftMC.races.Action.Trigger;
import de.dosmike.sponge.WarCraftMC.races.ActionData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

/** This will handle events we send, basically to prettify the code a bit */
public class WarCraftEventListeners {

	@Listener(order=Order.LAST)
	public void onGainXP(GainXPEvent event) {
		if (event.isCancelled()) return;
		Optional<Player> player = Sponge.getServer().getPlayer(event.getWarCraftProfile().getPlayerID());
//		Optional<Living> target = event.getCause().get(NamedCause.HIT_TARGET, Living.class);
//		Optional<CommandSource> something = event.getCause().get(NamedCause.SOURCE, CommandSource.class);
		if (player.isPresent()) {
			player.get().sendMessage(Text.of(TextColors.GREEN, "[WC] +"+event.getXP()+" XP"));
		}
	}
	
	@Listener(order=Order.LAST)
	public void onLevelUp(LevelUpEvent event) {
		if (event.isCancelled()) return;
		Optional<Player> player = Sponge.getServer().getPlayer(event.getWarCraftProfile().getPlayerID());
		if (player.isPresent())
			WarCraft.tell(player.get(), TextColors.GREEN, "You are now level " + event.getFinalLevel());
	}
	
	
	@Listener
	public void onStateChange(ProfileStateChangeEvent event) {
		if (!event.isProfileActive()) { //treat as if player disconnected
			if (!event.getPlayer().isPresent())
				WarCraft.w("We lost a player to space-time, effect could not be stripped on profile deactivate");
			else {
//				WarCraft.l(event.getPlayer().get() + " stopped playing WarCraft");
				PlayerStateManager.resetPlayerFx(event.getPlayer().get(), event.getWarCraftProfile());
				event.getWarCraftProfile().saveAndUnload();
			}
		} else {
			NextSpawnActionManager.onSpawn(event.getPlayer().get());
//			WarCraft.l(event.getPlayer().get() + " is now playing WarCraft");
			//treat as if player spawned
			XPpipe.archiveVanilla(event.getPlayer().get(), event.getWarCraftProfile());
			ActionData data = ActionData.builder(Trigger.ONSPAWN)
					.setSelf(event.getPlayer().get())
					.build();
			event.getWarCraftProfile().getRaceData().get().fire(event.getWarCraftProfile(), data);
			ManaPipe.resetMana(event.getPlayer().get());
		}
	}
}
