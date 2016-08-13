package io.github.mosadie.DeathSwap.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandDeathSwap implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		switch (args[0].toLowerCase()) {
		case "start":
			//TODO logic to start game
			return true;
		case "setstreamer":
			//TODO Set streaming player
		default:
			return false;
		}
	}

}
