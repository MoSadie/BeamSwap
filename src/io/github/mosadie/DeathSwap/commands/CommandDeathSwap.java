package io.github.mosadie.DeathSwap.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.mosadie.DeathSwap.DeathSwap;

public class CommandDeathSwap implements CommandExecutor{
	private DeathSwap ds;
	private String[] help = {"How to use the DeathSwap command:","/deathswap start [space seperated list of players]", "Use this command to start a game of death swap. If you want everyone on the server to play, you do not need to include the player list"," ","/deathswap stop", "Use this command to instantly stop the game."," ","/deathswap setstreamer [player]","Used to set the player that is streaming the game on Mixer, can be a spectator or a player in the game","","/deathswap trigger <swap or lava or resistance or food>","Used to manually trigger the Mixer button presses, mostly for debugging but trolling works as well.","","/deathswap help","Displays this message."};

	public CommandDeathSwap(DeathSwap ds) {
		this.ds = ds;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!sender.isOp() && sender instanceof Player) {
			sender.sendMessage("You need to be OP to use this command!");
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(help);
			return true;
		}
		switch (args[0].toLowerCase()) {
		case "start":
			if (ds.getServer().getOnlinePlayers().size() < 2) {
				sender.sendMessage("There are not enough players! You need at least 2 players to play!");
				return true;
			}
			if (args.length == 1) {
				ds.fighters.addAll( ds.getServer().getOnlinePlayers());
				ds.startGame();
				return true;
			}
			if (args.length == 2) {
				sender.sendMessage("Usage: /deathswap start <list of players seperated by spaces>");
				return true;
			}
			if (args.length > 3) {
				ArrayList<Player> players = new ArrayList<Player>();
				for (int i = 1; i < args.length; i++) {
					Player player = ds.getServer().getPlayer(args[i]);
					if (player == null) {
						sender.sendMessage("One of your player's names is incorrect. Please double check your spelling.");
						return true;
					} else {
						players.add(player);
					}
				}
				ds.startGame();
				return true;
			}

		case "stop":
			if (ds.inGame) ds.resetGame();
			else sender.sendMessage("Game not currently running.");
			return true;

		case "setstreamer":
			if (ds.mb.isPresent() == false) {
				sender.sendMessage("MixBukkit is not installed! Not setting streaming player!");
				return true;
			}
			if (args.length < 2 && sender instanceof Player) {
				ds.setStreamer((Player)sender);
				sender.sendMessage("You have been set as the streaming player! To set it to someone else, type /deathswap setstreamer <player>");
				return true;
			} else if (Bukkit.getServer().getPlayer(args[1]) != null) {
				ds.streamingPlayer = Bukkit.getServer().getPlayer(args[1]);
				sender.sendMessage("You have set the streaming player to " + args[1] +"!");
				return true;
			}
			sender.sendMessage("Unable to set steaming player.");
			return false;
		case "help":
			sender.sendMessage(help);
			return true;
		case "debug":
			sender.sendMessage("In Game: " +ds.inGame);
			sender.sendMessage("Countdown: "+ds.countdown.getCountdown());
			if (ds.streamingPlayer != null) sender.sendMessage("Streaming Player: " + ds.streamingPlayer.getDisplayName());
			for (int i = 0; i < ds.fighters.size(); i++) {
				if (ds.fighters.get(i) != null) sender.sendMessage("Fighter "+i+": "+ds.fighters.get(i).getPlayerListName());
			}
			return true;
		case "trigger":
			if (args.length < 2 || args.length > 2) {
				sender.sendMessage("/deathswap trigger <swap or lava or resistance or food>");
				return true;
			}
			switch(args[1].toLowerCase()) {
			case "swap": //Swap Early
				sender.sendMessage("Swap Early triggered!");
				if (ds.inGame) ds.swap();
				break;
			case "lava": //Give Lava Bucket
				sender.sendMessage("Give Lava triggered!");
				if (ds.inGame) ds.giveItem(new ItemStack(Material.LAVA_BUCKET,1), "lava bucket");
				break;
			case "resistance": //Make Invulnerable
				sender.sendMessage("Resistance triggered!");
				if (ds.inGame) ds.givePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,120,6), "invulnerability for 6 seconds");
				break;
			case "food": //Instant food and hunger refill
				sender.sendMessage("Feed triggered!");
				if (ds.inGame) {
					ds.giveItem(new ItemStack(Material.COOKED_BEEF,64), "cooked beef");
					ds.givePotionEffect(new PotionEffect(PotionEffectType.SATURATION,1,20),"the pleasent feeling of not being hungry");
				}
				break;
			case "spawnmob": //Spawn Mob
				sender.sendMessage("Spawn Mob triggered!");
				if (ds.inGame) {
					if (ds.isStreamerInGame()) {
						ds.spawnMob(ds.streamingPlayer);
					} else {
						ds.spawnMob();
					}
				}
				break;
			default:
				sender.sendMessage("/deathswap trigger <swap or lava or resistance or food or spawnmob>");
				break;
			}
			return true;
		default:
			ds.getLogger().info("Unknown Command " + args[0]);
			sender.sendMessage("Unknown command. Try /deathswap help!");
			return true;
		}
	}

}
