package io.github.mosadie.BeamSwap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class Game extends Thread{
	public boolean kill = false;
	private BeamSwap bs;
	public Player streamingPlayer;
	public Player[] fighters;
	public Location[] origLocation;
	public boolean inGame = false;
	public int swapTime; //in seconds
	private Scoreboard board;
	
	public Game (BeamSwap bs) {
		this.bs = bs;
	}

	public void run() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		 
		Objective objective = board.registerNewObjective("showhealth", "health");
		objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		bs.getLogger().info("Game thread loaded.");
		while (!kill) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void startGame() {
		origLocation[0] = fighters[0].getLocation();
		origLocation[1] = fighters[1].getLocation();
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "achivement take * " + fighters[0]);
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "achivement take * " + fighters[1]);
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "spreadplayers 0 0 10000000 15000000 false " + fighters[0].getPlayerListName()+ " " + fighters[1].getPlayerListName());
		for (int i =0; i < fighters.length; i++) {
			fighters[i].setGameMode(GameMode.SURVIVAL);
			fighters[i].setScoreboard(board);
		}
		inGame = true;
		bs.updateState("in-game");//TODO Check name
		Bukkit.getServer().broadcastMessage("Let the death swap begin!");
		while (fighters[0].getHealth() > 0 && fighters[1].getHealth() > 0) {
			Player[] online = (Player[]) Bukkit.getOnlinePlayers().toArray();
			for (int i = 0; i < online.length; i++) {
				if (online[i] != fighters[0] && online[i] != fighters[1] && !online[i].getGameMode().equals(GameMode.SPECTATOR)) {
					online[i].setGameMode(GameMode.SPECTATOR);
				}
			}
			online = null;
			if (swapTime <= 0) {
				bs.swap();
			}
			swapTime--;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bs.getLogger().info("End of match!");
		//End of match!
		if (fighters[0].getHealth() == 0 && fighters[1].getHealth() == 0) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {text:\"It's a tie!\",color:\"dark_purple\",bold:true,italic:true,underlined:true}");
		}
		else if (fighters[0].getHealth() == 0) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {text:\"" + fighters[1] + " Wins!\",color:\"dark_purple\",bold:true,italic:true,underlined:true}");
		} else if (fighters[1].getHealth() == 0) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {text:\"" + fighters[0] + " Wins!\",color:\"dark_purple\",bold:true,italic:true,underlined:true}");
		}
		for (int i = 0; i<fighters.length;i++) {
				fighters[i].teleport(origLocation[i]);
			}
		Player[] finalOnline = (Player[]) Bukkit.getOnlinePlayers().toArray();
		for (int i = 0; i < finalOnline.length;i++) {
			if (finalOnline[i].getGameMode() == GameMode.SPECTATOR) finalOnline[i].setGameMode(GameMode.CREATIVE);
			finalOnline[i].teleport(origLocation[0]);
		}
	}
	
	public void setStreamer(Player streamer) {
		streamingPlayer = streamer;
	}
}
