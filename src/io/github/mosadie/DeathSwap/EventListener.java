package io.github.mosadie.DeathSwap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.mosadie.MixBukkit.events.ControlMouseDownInput;

public class EventListener implements Listener {
	private DeathSwap ds;
	public EventListener(DeathSwap ds) {
		this.ds = ds;
	}
	
	@EventHandler
	public void OnPlayerLeave(PlayerQuitEvent event) {
		if (ds.streamingPlayer == event.getPlayer()) ds.streamingPlayer = null;
		if (ds.inGame) {
			ds.fighters.remove(event.getPlayer());
			ds.fighters.trimToSize();
			if (ds.fighters.size() == 1) {
				ds.win(ds.fighters.get(0));
			} else if (ds.fighters.size() < 1) {
				ds.resetGame();
			}
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent pde) {
		Player deadPlayer = pde.getEntity();
		if (!ds.inGame) return;
		ds.fighters.remove(deadPlayer);
		ds.fighters.trimToSize();
		deadPlayer.setGameMode(GameMode.SPECTATOR);
		if (ds.fighters.size() <= 1) {
			ds.win(ds.fighters.get(0));
		}
	}

}
