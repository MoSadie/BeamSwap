package io.github.mosadie.DeathSwap.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.mosadie.DeathSwap.DeathSwap;

public class CommandDeathSwap implements CommandExecutor{
	private DeathSwap ds;
	private String[] help = {"How to use the DeathSwap command:","/deathswap start [player1] [player2]", "Use this command to start a game of death swap. If you only have two players on your server, you do not need to include player one or two."," ","/deathswap setstreamer [player]","Used to set the player that is streaming the game on Beam, can be a spectator or a player in the game","","/deathswap help","Displays this message."};
	
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
			if (args.length > 3 || args.length == 2) {
				sender.sendMessage("Usage: /beamswap start <player1> <player2>");
				return true;
			}
			
			if (ds.getServer().getOnlinePlayers().size()<2) {
				sender.sendMessage("There are not enough players! You need 2 players to play!");
				return true;
			}
			
			if (ds.getServer().getOnlinePlayers().size() == 2) {
				ds.fighters = ds.getServer().getOnlinePlayers().toArray(new Player[2]);
				ds.getLogger().info(ds.fighters[0].toString());
				ds.getLogger().info(ds.fighters[1].toString());
				ds.startGame();
				return true;
			}
			if (ds.getServer().getPlayer(args[1]) == null || ds.getServer().getPlayer(args[2]) == null) {
				sender.sendMessage("One of your player's names is incorrect. Please double check your spelling.");
				return true;
			}
			ds.fighters[0] = ds.getServer().getPlayer(args[1]);
			ds.fighters[1] = ds.getServer().getPlayer(args[2]);
			ds.startGame();
			
			return true;
		case "setstreamer":
			if (ds.bb.isPresent() == false) {
				sender.sendMessage("BeamBukkit is not installed! Not setting streaming player!");
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
			if (ds.streamingPlayer != null) sender.sendMessage("Streaming Player: " + ds.streamingPlayer.getDisplayName());
			if (ds.fighters[0] != null) sender.sendMessage("Fighter 0: " +ds.fighters[0].toString());
			if (ds.fighters[1] != null) sender.sendMessage("Fighter 1: "+ds.fighters[1].toString());
			return true;
		default:
			ds.getLogger().info("Unknown Command " + args[0]);
			sender.sendMessage("Unknown command. Try /deathswap help!");
			return false;
		}
	}

}
