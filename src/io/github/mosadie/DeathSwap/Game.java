package io.github.mosadie.DeathSwap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class Game extends Thread{
	private DeathSwap ds;
	public Player streamingPlayer;
	public Player[] fighters;
	public boolean inGame = false;
	public int swapTime; //in seconds
	
	public Game (DeathSwap ds) {
		this.ds = ds;
	}

	public void run() {
		ds.getLogger().info("Game thread loaded.");
	}

	public void startGame() {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "achivement take * " + fighters[0]);
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "achivement take * " + fighters[1]);
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "spreadplayers 0 0 10000000 15000000 false " + fighters[0].getPlayerListName()+ " " + fighters[1].getPlayerListName());
		inGame = true;
		ds.updateState("In-Game");//TODO Check name
		Bukkit.getServer().broadcastMessage("Let the Death Swap begin!");
		while (fighters[0].getHealth() > 0 && fighters[1].getHealth() > 0) {
			Player[] online = (Player[]) Bukkit.getOnlinePlayers().toArray();
			for (int i = 0; i < online.length; i++) {
				if (online[i] != fighters[0] && online[i] != fighters[1] && !online[i].getGameMode().equals(GameMode.SPECTATOR)) {
					online[i].setGameMode(GameMode.SPECTATOR);
				}
			}
			if (swapTime <= 0) {
				ds.swap();
			}
			swapTime--;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
