package io.github.mosadie.DeathSwap.commands;

import java.util.concurrent.ExecutionException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import io.github.mosadie.DeathSwap.DeathSwap;
import pro.beam.interactive.robot.RobotBuilder;

public class CommandDsauth  implements CommandExecutor{
	DeathSwap ds;
	public CommandDsauth(DeathSwap ds) {
		this.ds = ds;	
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (args[0].length() == 6) {
			ds.getLogger().info("Attempting to use two-factor auth...");
			try {
				ds.beamRobot = new RobotBuilder()
						.username(ds.config.getString("beam_username"))
						.password(ds.config.getString("beam_password"))
						.channel(ds.config.getInt("beam_channelid"))
						.twoFactor(args[0])
						.build(ds.beam).get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else {
			return false;
		}
	}

}
