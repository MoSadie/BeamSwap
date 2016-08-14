package io.github.mosadie.DeathSwap.commands;

import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.github.mosadie.DeathSwap.DeathSwap;
import pro.beam.interactive.robot.RobotBuilder;

public class CommandDeathSwap implements CommandExecutor{
	private DeathSwap ds;
	private String[] help = {"How to use the DeathSwap command:","/deathswap start [player1] [player2]", "Use this command to start a game of death swap. If you only have two players on your server, you do not need to include player one or two."," ","/deathswap setstreamer [player]","Used to set the player that is streaming the game on Beam, can be a spectator or a player in the game","","/deathswap auth <6 character code>","Used to enable the Beam robot if the streamer has two-factor auth enabled. Make sure to check the config file.","","/deathswap help","Displays this message."};
	
	public CommandDeathSwap(DeathSwap ds) {
		this.ds = ds;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("You need to be OP to use this command!");
			return false;
		}
		switch (args[0].toLowerCase()) {
		case "start":
			//TODO logic to start game
			return true;
		case "setstreamer":
			if (args[1] == null && sender instanceof Player) {
				ds.game.streamingPlayer = (Player)sender;
				return true;
			} else if (Bukkit.getServer().getPlayer(args[1]) != null) {
				ds.game.streamingPlayer = Bukkit.getServer().getPlayer(args[1]);
				return true;
			}
			sender.sendMessage("Unable to set steaming player.");
			return false;
		case "auth":
			if (args[1].length() == 6 && ds.beamRobot != null) {
				ds.getLogger().info("Attempting to use two-factor auth...");
				try {
					ds.beamRobot = new RobotBuilder()
							.username(ds.config.getString("beam_username"))
							.password(ds.config.getString("beam_password"))
							.channel(ds.config.getInt("beam_channelid"))
							.twoFactor(args[1])
							.build(ds.beam).get();
					
					ds.startBeamRobot();
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
			sender.sendMessage("Unknown command. Try /deathswap help!");
			return false;
		}
	}

}
