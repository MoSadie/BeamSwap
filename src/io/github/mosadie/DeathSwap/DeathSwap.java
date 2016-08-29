package io.github.mosadie.DeathSwap;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import io.github.mosadie.BeamBukkit.BeamBukkit;
import io.github.mosadie.BeamBukkit.ReportEvent;
import io.github.mosadie.DeathSwap.commands.CommandDeathSwap;
import pro.beam.api.BeamAPI;
import pro.beam.interactive.net.packet.Protocol;
import pro.beam.interactive.net.packet.Protocol.Report;
import pro.beam.interactive.net.packet.Protocol.Report.TactileInfo;
import pro.beam.interactive.robot.Robot;
import pro.beam.interactive.robot.RobotBuilder;

public class DeathSwap extends JavaPlugin {
	public FileConfiguration config = this.getConfig();
	public BeamAPI beam;
	public Robot beamRobot;
	public BeamBukkit bb = null;
	public Player streamingPlayer;
	public Player[] fighters;
	public Location[] origLocation;
	public boolean inGame = false;
	public int swapTime; //in seconds
	private Scoreboard board;
	private BukkitTask countdown;

	@Override
	public void onEnable() {
		this.getCommand("deathswap").setExecutor(new CommandDeathSwap(this));
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		 
		Objective objective = board.registerNewObjective("showhealth", "health");
		objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
	}

	@EventHandler
	public void onReport(ReportEvent re) {
		if (bb == null) bb = re.getBeamBukkit();
		Report report = re.getReport(); 
		if (report.getTactileCount() > 0) {
			for (int i = 0; i < report.getTactileList().size();i++) {
				TactileInfo button = report.getTactile(i);
				if (button.getPressFrequency() > 1) {
					switch(button.getId()) {
					case 0: //Swap Early
						if (inGame) swap();
						break;
					case 1: //Give Lava Bucket
						if (inGame) giveItem(new ItemStack(Material.LAVA_BUCKET,1));
						break;
					case 2: //Make Invulnerable
						if (inGame) givePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,60,6));
						break;
					case 3: //Instant food and hunger refill
						if (inGame) {
							giveItem(new ItemStack(Material.COOKED_BEEF,64));
							givePotionEffect(new PotionEffect(PotionEffectType.SATURATION,1,20));
						}
						break;
					}
				}
			}
		}
	}

	@Override
	public void onDisable() {
		countdown.cancel();
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
		bb.updateState("in-game");//TODO Check name
		Bukkit.getServer().broadcastMessage("Let the death swap begin!");
		countdown = new CountDownTask(this, genRandomTime()).runTaskTimer(this,20,20);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent pde) {
		Player deadPlayer = pde.getEntity();
		if (deadPlayer != fighters[0] && deadPlayer != fighters[1]) return;
		getLogger().info("End of match!");
		//End of match!
		countdown.cancel();
		if (fighters[0] == deadPlayer) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {text:\"" + fighters[1] + " Wins!\",color:\"dark_purple\",bold:true,italic:true,underlined:true}");
		} else if (fighters[1] == deadPlayer) {
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
		if (game.inGame) {
			if (game.streamingPlayer != null) {
				if (game.fighters[0] == game.streamingPlayer | game.fighters[1] == game.streamingPlayer) {
					game.streamingPlayer.getInventory().addItem(itemToGive);
					game.streamingPlayer.chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
				}
				else {
					game.fighters[0].getInventory().addItem(itemToGive);
					game.fighters[0].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
					game.fighters[1].getInventory().addItem(itemToGive);
					game.fighters[1].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
				}
			} else {
				game.fighters[0].getInventory().addItem(itemToGive);
				game.fighters[0].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
				game.fighters[1].getInventory().addItem(itemToGive);
				game.fighters[1].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
			}
		}
	}

	public void givePotionEffect(PotionEffect effectToGive) {
		if (game.inGame) {
			if (game.streamingPlayer != null) {
				if (game.fighters[0] == game.streamingPlayer | game.fighters[1] == game.streamingPlayer) {
					game.streamingPlayer.addPotionEffect(effectToGive);
					game.streamingPlayer.chat("You have been gifted " + effectToGive.toString() + " by the stream!");
				}
				else {
					game.fighters[0].addPotionEffect(effectToGive);
					game.fighters[0].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
					game.fighters[1].addPotionEffect(effectToGive);
					game.fighters[1].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
				}
			} else {
				game.fighters[0].addPotionEffect(effectToGive);
				game.fighters[0].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
				game.fighters[1].addPotionEffect(effectToGive);
				game.fighters[1].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
			}
		}
	}
	
	public void setStreamer(Player streamer) {
		streamingPlayer = streamer;
	}
	
	public int genRandomTime() {
		return 30 + (int)(Math.random() * ((90 - 30) + 1));
	}
}
