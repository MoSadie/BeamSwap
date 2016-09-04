package io.github.mosadie.DeathSwap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
	private DeathSwap ds;
	
	public DeathListener(DeathSwap ds) {
		this.ds = ds;
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent pde) {
		Player deadPlayer = pde.getEntity();
		if (!ds.inGame) return;
		if (deadPlayer != ds.fighters[0] && deadPlayer != ds.fighters[1]) return;
		ds.getLogger().info("End of match!");
		//End of match!
		ds.countdown.cancel();
		ds.getLogger().info("Loser: "+deadPlayer.getDisplayName()+"!");
		if (ds.fighters[0] == deadPlayer) {
			ds.getLogger().info("Winner: " + ds.fighters[1].getDisplayName() + "!");
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + ds.fighters[1].getPlayerListName() + " Wins!\",\"color\":\"dark_purple\",\"bold\":true,\"italic\":true,\"underlined\":true}");
		} else if (ds.fighters[1] == deadPlayer) {
			ds.getLogger().info("Winner: " + ds.fighters[0].getDisplayName() + "!");
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + ds.fighters[0].getPlayerListName() + " Wins!\",\"color\":\"dark_purple\",\"bold\":true,\"italic\":true,\"underlined\":true}");
		}

		Player[] finalOnline = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (int i = 0; i < finalOnline.length;i++) {
			if (finalOnline[i].getGameMode() == GameMode.SPECTATOR) finalOnline[i].setGameMode(GameMode.CREATIVE);
			finalOnline[i].teleport(ds.origPlayer1);
		}
		ds.fighters[1].teleport(ds.origPlayer2);
		ds.resetGame();
	}

}
