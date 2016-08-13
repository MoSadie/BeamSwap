package io.github.mosadie.DeathSwap;

import org.bukkit.entity.Player;

public class Game extends Thread{
	private DeathSwap ds;
	public Player streamingPlayer;
	public Game (DeathSwap ds) {
		this.ds = ds;
	}
}
