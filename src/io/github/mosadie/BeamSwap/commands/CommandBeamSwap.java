package io.github.mosadie.BeamSwap.commands;

import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.mosadie.BeamSwap.BeamSwap;
import pro.beam.interactive.robot.RobotBuilder;

public class CommandBeamSwap implements CommandExecutor{
	private BeamSwap bs;
	private String[] help = {"How to use the BeamSwap command:","/beamswap start [player1] [player2]", "Use this command to start a game of beam swap. If you only have two players on your server, you do not need to include player one or two."," ","/beamswap setstreamer [player]","Used to set the player that is streaming the game on Beam, can be a spectator or a player in the game","","/beamswap auth <6 character code>","Used to enable the Beam robot if the streamer has two-factor auth enabled. Make sure to check the config file.","","/beamswap help","Displays this message."};
	
	public CommandBeamSwap(BeamSwap bs) {
		this.bs = bs;
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
			if (bs.getServer().getOnlinePlayers().size() == 2) {
				bs.game.fighters = (Player[]) bs.getServer().getOnlinePlayers().toArray();
				bs.game.startGame();
			}
			if (bs.getServer().getPlayer(args[1]) == null || bs.getServer().getPlayer(args[2]) == null) {
				sender.sendMessage("One of your player's names is incorrect. Please double check your spelling.");
				return true;
			}
			bs.game.fighters[0] = bs.getServer().getPlayer(args[1]);
			bs.game.fighters[1] = bs.getServer().getPlayer(args[2]);
			
			return true;
		case "setstreamer":
			if (args.length < 2 && sender instanceof Player) {
				bs.game.setStreamer((Player)sender);
				sender.sendMessage("You have been set as the streaming player! To set it to someone else, type /beamswap setstreamer <player>");
				return true;
			} else if (Bukkit.getServer().getPlayer(args[1]) != null) {
				bs.game.streamingPlayer = Bukkit.getServer().getPlayer(args[1]);
				sender.sendMessage("You have set the streaming player to " + args[1] +"!");
				return true;
			}
			sender.sendMessage("Unable to set steaming player.");
			return false;
		case "auth":
			if (args[1].length() == 6 && bs.beamRobot != null) {
				bs.getLogger().info("Attempting to use two-factor auth...");
				try {
					bs.beamRobot = new RobotBuilder()
							.username(bs.config.getString("beam_username"))
							.password(bs.config.getString("beam_password"))
							.channel(bs.config.getInt("beam_channelid"))
							.twoFactor(args[1])
							.build(bs.beam).get();
					
					bs.startBeamRobot();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			} else {
				sender.sendMessage("The auth code is not formatted correctly or the Beam Robot already exists.");
				return false;
			}
		case "help":
			sender.sendMessage(help);
			return true;
		default:
			bs.getLogger().info("Unknown Command " + args[0]);
			sender.sendMessage("Unknown command. Try /deathswap help!");
			return false;
		}
	}

}
