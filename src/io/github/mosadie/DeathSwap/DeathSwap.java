package io.github.mosadie.DeathSwap;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.scene.InteractiveScene;
import com.mixer.interactive.services.GroupServiceProvider;

import io.github.mosadie.MixBukkit.MixBukkit;
import io.github.mosadie.DeathSwap.commands.CommandDeathSwap;

public class DeathSwap extends JavaPlugin implements Listener {
	public Optional<MixBukkit> mb = Optional.empty();
	public Player streamingPlayer;
	public ArrayList<Player> fighters = new ArrayList<Player>();
	public ArrayList<Location> origPlayer = new ArrayList<Location>();
	public boolean inGame = false;
	private Scoreboard board;
	public CountDownTask countdown;
	public BukkitTask countdownTask;
	public EventListener eventListener;
	public MixerListener mixerListener;

	private InteractiveGroup group;


	@Override
	public void onEnable() {
		mb = Optional.ofNullable((MixBukkit)getServer().getPluginManager().getPlugin("MixBukkit"));
		this.getCommand("deathswap").setExecutor(new CommandDeathSwap(this));
		getServer().getPluginManager().registerEvents(eventListener = new EventListener(this), this);
		if (mb.isPresent()) {
			if (mb.get().config.getInt("mixer_project_version") != 69329) {
				getLogger().warning("Not enabling Mixer integration. MixBukkit configured with wrong project version. Please set it to 69329 and reload.");
				mb = Optional.empty();
			}
			if (mb.isPresent()) {
				group = new InteractiveGroup("default");
				getServer().getPluginManager().registerEvents(mixerListener = new MixerListener(this), this);
			}
		}

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
		for(int i = 0; i < fighters.size(); i++) {
			origPlayer.add(fighters.get(i).getLocation());
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + fighters.get(i).getPlayerListName()+" everything");
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "recipe take " + fighters.get(i).getPlayerListName()+" *");
			fighters.get(i).setHealth(fighters.get(i).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			fighters.get(i).setFoodLevel(20);
			fighters.get(i).setSaturation(20);
		}
		String command = "spreadplayers 0 0 100000 500000 false";
		for(int i = 0; i < fighters.size(); i++) {
			command = command.concat(" "+fighters.get(i).getPlayerListName());
		}
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
		for (int i =0; i < fighters.size(); i++) {
			fighters.get(i).setGameMode(GameMode.SURVIVAL);
			fighters.get(i).setScoreboard(board);
			fighters.get(i).getInventory().clear();
		}
		inGame = true;
		if (mb.isPresent()) mb.get().setScene(new InteractiveScene("in-game"), group);
		Bukkit.getServer().broadcastMessage("Let the death swap begin!");
		countdown = new CountDownTask(this, genRandomTime());
		countdownTask = countdown.runTaskTimer(this,20,20);
	}

	public void swap() {
		swap(false);
	}

	public void swap(boolean byStream) {
		if (inGame) {
			countdown.cancel();
			ArrayList<Location> tmpLoc = new ArrayList<Location>();
			for (int i = 0; i < fighters.size(); i++) {
				tmpLoc.add(fighters.get(i).getLocation());
			}
			for (int i = 0; i < (fighters.size()-1); i++) {
				fighters.get(i).teleport(tmpLoc.get(i+1));
			}
			fighters.get(fighters.size()-1).teleport(tmpLoc.get(0));
			if (!byStream) Bukkit.broadcastMessage("SWAPPED!");
			else Bukkit.broadcastMessage("SWAPPED BY THE STREAM!");
			countdown = new CountDownTask(this,genRandomTime());
			countdownTask = countdown.runTaskTimer(this,20,20);
		}
	}

	public void giveItem(ItemStack itemToGive, String fancyName) {
		if (inGame && mb.isPresent()) {
			if (streamingPlayer != null) {
				if (isStreamerInGame()) {
					streamingPlayer.getInventory().addItem(itemToGive);
					streamingPlayer.sendMessage("You have been gifted " + itemToGive.getAmount() + " " + fancyName+ " by the stream!");
				}
				else {
					for (int i = 0; i < fighters.size(); i++) {
						fighters.get(i).getInventory().addItem(itemToGive);
						fighters.get(i).sendMessage("You have been gifted " + itemToGive.getAmount() + " " + fancyName + " by the stream!");

					}
				}
			} else {
				for(int i = 0; i < fighters.size(); i++) {
					fighters.get(i).getInventory().addItem(itemToGive);
					fighters.get(i).sendMessage("You have been gifted " + itemToGive.getAmount() + " " + fancyName + " by the stream!");
				}
			}
		}
	}

	public void givePotionEffect(PotionEffect effectToGive, String fancyName) {
		if (inGame && mb.isPresent()) {
			if (streamingPlayer != null) {
				if (isStreamerInGame()) {
					new SyncPlayerTask(streamingPlayer,effectToGive).runTaskLater(this, 1);
					//streamingPlayer.addPotionEffect(effectToGive);
					streamingPlayer.sendMessage("You have been gifted " + fancyName + " by the stream!");
				}
				else {
					for(int i = 0; i < fighters.size(); i++) {
						new SyncPlayerTask(fighters.get(i),effectToGive).runTaskLater(this, 1);
						fighters.get(i).sendMessage("You have been gifted " + fancyName + " by the stream!");
					}
				}
			} else {
				for(int i = 0; i < fighters.size(); i++) {
					new SyncPlayerTask(fighters.get(i),effectToGive).runTaskLater(this, 1);
					fighters.get(i).sendMessage("You have been gifted " + fancyName + " by the stream!");
				}
			}
		}
	}

	public void spawnMob() {
		for(int i = 0; i < fighters.size(); i++) {
			spawnMob(fighters.get(i));
		}
	}

	public static final EntityType[] evilMobs = { EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.WITCH, EntityType.ZOMBIE };
	public void spawnMob(Player playerToSpawnAround) {
		EntityType entityToSpawn = evilMobs[new Random().nextInt(evilMobs.length)];
		new SyncMobSpawnTask(playerToSpawnAround.getLocation(), entityToSpawn, playerToSpawnAround.getWorld()).runTaskLater(this, 1);
		playerToSpawnAround.sendMessage("You have been gifted a random hostile mob spawn on you!");
	}

	public void win(Player winner) {
		getLogger().info("End of match!");
		//End of match!
		getLogger().info("Winner: " + winner.getDisplayName() + "!");
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + winner.getDisplayName() + " Wins!\",\"color\":\"dark_purple\",\"bold\":true,\"italic\":true,\"underlined\":true}");
		resetGame();

	}

	public void setStreamer(Player streamer) {
		if (mb.isPresent()) streamingPlayer = streamer;
	}

	public boolean isStreamerInGame() {
		for(int i = 0; i < fighters.size(); i++) {
			if (streamingPlayer == fighters.get(i)) 
				return true;
		}
		return false;
	}

	public int genRandomTime() {
		return 30 + (int)(Math.random() * ((90 - 30) + 1));
	}

	public void resetGame() {
		inGame = false;
		countdown.cancel();
		Player[] finalOnline = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		for (int i = 0; i < finalOnline.length;i++) {
			finalOnline[i].getInventory().clear();
			finalOnline[i].setHealth(finalOnline[i].getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			finalOnline[i].setFoodLevel(20);
			finalOnline[i].setSaturation(20);
			if (finalOnline[i].getGameMode() == GameMode.SPECTATOR) finalOnline[i].setGameMode(GameMode.CREATIVE);
			finalOnline[i].teleport(origPlayer.get(0));
		}
		fighters.clear();
		if (mb.isPresent()) mb.get().setScene(new InteractiveScene("default"), group);
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

class SyncMobSpawnTask extends BukkitRunnable {
	Location loc;
	EntityType type;
	World world;

	SyncMobSpawnTask(Location location, EntityType entityType, World worldToSpawnIn) {
		loc = location;
		type = entityType;
		world = worldToSpawnIn;
	}

	public void run() {
		world.spawnEntity(loc, type);
	}
}
