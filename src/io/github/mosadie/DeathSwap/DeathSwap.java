package io.github.mosadie.DeathSwap;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import io.github.mosadie.BeamBukkit.BeamBukkit;
import io.github.mosadie.DeathSwap.commands.CommandDeathSwap;

public class DeathSwap extends JavaPlugin implements Listener {
	public Optional<BeamBukkit> bb = Optional.empty();
	public Player streamingPlayer;
	public Player[] fighters = {null,null};
	public Location origPlayer1;
	public Location origPlayer2;
	public boolean inGame = false;
	public int swapTime; //in seconds
	private Scoreboard board;
	BukkitTask countdown;

	@Override
	public void onEnable() {
		bb = Optional.ofNullable((BeamBukkit)getServer().getPluginManager().getPlugin("BeamBukkit"));
		this.getCommand("deathswap").setExecutor(new CommandDeathSwap(this));
		getServer().getPluginManager().registerEvents(new DeathListener(this), this);
		if (bb.isPresent()) getServer().getPluginManager().registerEvents(new ReportListener(this), this);
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		 
		Objective objective = board.registerNewObjective("showhealth", "health");
		objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
	}

	@Override
	public void onDisable() {
		if (countdown != null) countdown.cancel();
	}
	
	public void startGame() {
		origPlayer1 = fighters[0].getLocation();
		origPlayer2 = fighters[1].getLocation();
		
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "achievement take * " + fighters[0].getDisplayName());
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "achievement take * " + fighters[1].getDisplayName());
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "spreadplayers 0 0 100000 15000000 false " + fighters[0].getPlayerListName()+ " " + fighters[1].getPlayerListName());
		for (int i =0; i < fighters.length; i++) {
			fighters[i].setGameMode(GameMode.SURVIVAL);
			fighters[i].setScoreboard(board);
			fighters[i].getInventory().clear();
		}
		inGame = true;
		if (bb.isPresent()) bb.get().updateState("in-game");
		Bukkit.getServer().broadcastMessage("Let the death swap begin!");
		countdown = new CountDownTask(this, genRandomTime()).runTaskTimer(this,20,20);
	}
	
	
	public void swap() {
		if (inGame) {
			countdown.cancel();
			Location player1tmp = fighters[0].getLocation();
			Location player2tmp = fighters[1].getLocation();
			fighters[0].teleport(player2tmp);
			fighters[1].teleport(player1tmp);
			Bukkit.broadcastMessage("SWAPPED!");
			swapTime = genRandomTime();
			countdown = new CountDownTask(this,genRandomTime()).runTaskTimer(this,20,20);
		}
	}

	public void giveItem(ItemStack itemToGive) {
		if (inGame && bb.isPresent()) {
			if (streamingPlayer != null) {
				if (fighters[0] == streamingPlayer | fighters[1] == streamingPlayer) {
					streamingPlayer.getInventory().addItem(itemToGive);
					streamingPlayer.sendMessage("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.getItemMeta().getDisplayName() + " by the stream!");
				}
				else {
					fighters[0].getInventory().addItem(itemToGive);
					fighters[0].sendMessage("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.getItemMeta().getDisplayName() + " by the stream!");
					fighters[1].getInventory().addItem(itemToGive);
					fighters[1].sendMessage("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.getItemMeta().getDisplayName() + " by the stream!");
				}
			} else {
				fighters[0].getInventory().addItem(itemToGive);
				fighters[0].sendMessage("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.getItemMeta().getDisplayName() + " by the stream!");
				fighters[1].getInventory().addItem(itemToGive);
				fighters[1].sendMessage("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.getItemMeta().getDisplayName() + " by the stream!");
			}
		}
	}

	public void givePotionEffect(PotionEffect effectToGive) {
		if (inGame && bb.isPresent()) {
			if (streamingPlayer != null) {
				if (fighters[0] == streamingPlayer | fighters[1] == streamingPlayer) {
					new SyncPlayerTask(streamingPlayer,effectToGive).runTaskLater(this, 1);
					//streamingPlayer.addPotionEffect(effectToGive);
					streamingPlayer.sendMessage("You have been gifted " + effectToGive.getType().getName() + " by the stream!");
				}
				else {
					new SyncPlayerTask(fighters[0],effectToGive).runTaskLater(this, 1);
					//fighters[0].addPotionEffect(effectToGive);
					fighters[0].sendMessage("You have been gifted " + effectToGive.getType().getName() + " by the stream!");
					new SyncPlayerTask(fighters[1],effectToGive).runTaskLater(this, 1);
					//fighters[1].addPotionEffect(effectToGive);
					fighters[1].sendMessage("You have been gifted " + effectToGive.getType().getName() + " by the stream!");
				}
			} else {
				new SyncPlayerTask(fighters[0],effectToGive).runTaskLater(this, 1);
				//fighters[0].addPotionEffect(effectToGive);
				fighters[0].sendMessage("You have been gifted " + effectToGive.getType().getName() + " by the stream!");
				new SyncPlayerTask(fighters[1],effectToGive).runTaskLater(this, 1);
				//fighters[1].addPotionEffect(effectToGive);
				fighters[1].sendMessage("You have been gifted " + effectToGive.getType().getName() + " by the stream!");
			}
		}
	}
	
	public void setStreamer(Player streamer) {
		if (bb.isPresent()) streamingPlayer = streamer;
	}
	
	public int genRandomTime() {
		return 30 + (int)(Math.random() * ((90 - 30) + 1));
	}
	
	public void resetGame() {
		inGame = false;
		fighters[0].getInventory().clear();
		fighters[0] = null;
		fighters[1].getInventory().clear();
		fighters[1] = null;
		if (bb.isPresent()) bb.get().updateState("default");
	}
}

class SyncPlayerTask extends BukkitRunnable {
    Player p;
    PotionEffect effect;
   
    SyncPlayerTask(Player player,PotionEffect potion) {
        p = player;
        effect = potion;
    }
       
    public void run() {           
        p.addPotionEffect(effect);
    }
}
