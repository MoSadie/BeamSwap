package io.github.mosadie.DeathSwap;

import org.bukkit.scheduler.BukkitRunnable;

public class CountDownTask extends BukkitRunnable {
	
	private final DeathSwap plugin;
	private int countdown;
	
	public CountDownTask(DeathSwap ds, int count) {
		plugin = ds;
		if (count > 0) countdown = count;
	}
	@Override
	public void run() {
		if (plugin.inGame == false) return;
		if (countdown < 1) plugin.swap();
		countdown--;
	}

}
