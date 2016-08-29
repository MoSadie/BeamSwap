package io.github.mosadie.DeathSwap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import io.github.mosadie.BeamBukkit.BeamBukkit;
import io.github.mosadie.BeamBukkit.ReportEvent;
import io.github.mosadie.DeathSwap.commands.CommandDeathSwap;
import pro.beam.interactive.net.packet.Protocol.Report;
import pro.beam.interactive.net.packet.Protocol.Report.TactileInfo;

public class DeathSwap extends JavaPlugin implements Listener {
	public BeamBukkit bb = null;
	public Player streamingPlayer;
	public Player[] fighters = {null,null};
	public Location origPlayer1;
	public Location origPlayer2;
	public boolean inGame = false;
	public int swapTime; //in seconds
	private Scoreboard board;
	private BukkitTask countdown;

	@Override
	public void onEnable() {
		this.getCommand("deathswap").setExecutor(new CommandDeathSwap(this));
		getServer().getPluginManager().registerEvents(this, this);
		
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
				if (button.getPressFrequency() >= 1) {
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
		getLogger().info(bb.toString());
		bb.updateState("in-game");//TODO Check name
		Bukkit.getServer().broadcastMessage("Let the death swap begin!");
		countdown = new CountDownTask(this, genRandomTime()).runTaskTimer(this,20,20);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent pde) {
		Player deadPlayer = pde.getEntity();
		if (!inGame) return;
		if (deadPlayer != fighters[0] && deadPlayer != fighters[1]) return;
		getLogger().info("End of match!");
		//End of match!
		countdown.cancel();
		getLogger().info("Loser: "+deadPlayer.getDisplayName()+"!");
		if (fighters[0] == deadPlayer) {
			getLogger().info("Winner: " + fighters[1].getDisplayName() + "!");
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + fighters[1].getPlayerListName() + " Wins!\",\"color\":\"dark_purple\",\"bold\":true,\"italic\":true,\"underlined\":true}");
		} else if (fighters[1] == deadPlayer) {
			getLogger().info("Winner: " + fighters[0].getDisplayName() + "!");
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + fighters[0].getPlayerListName() + " Wins!\",\"color\":\"dark_purple\",\"bold\":true,\"italic\":true,\"underlined\":true}");
		}

		Player[] finalOnline = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (int i = 0; i < finalOnline.length;i++) {
			if (finalOnline[i].getGameMode() == GameMode.SPECTATOR) finalOnline[i].setGameMode(GameMode.CREATIVE);
			finalOnline[i].teleport(origPlayer1);
		}
		fighters[1].teleport(origPlayer2);
		inGame = false;
		fighters[0].getInventory().clear();
		fighters[0] = null;
		fighters[0].getInventory().clear();
		fighters[1] = null;
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
		if (inGame) {
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
		if (inGame) {
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
		streamingPlayer = streamer;
	}
	
	public int genRandomTime() {
		return 30 + (int)(Math.random() * ((90 - 30) + 1));
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
