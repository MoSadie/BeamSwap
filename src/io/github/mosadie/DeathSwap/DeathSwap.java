package io.github.mosadie.DeathSwap;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pro.beam.api.BeamAPI;
import pro.beam.interactive.net.packet.Protocol;
import pro.beam.interactive.net.packet.Protocol.Report.TactileInfo;
import pro.beam.interactive.robot.Robot;
import pro.beam.interactive.robot.RobotBuilder;

public class DeathSwap extends JavaPlugin {
	public FileConfiguration config = this.getConfig();
	public BeamAPI beam;
	public Robot beamRobot;
	public Game game;
	
	@Override
	public void onEnable() {
		config.addDefault("useBeam", false);
		config.addDefault("beam_username", "Username");
		config.addDefault("beam_password", "Password");
		config.addDefault("beam_twofactorrequired", false);
		config.addDefault("beam_channelid", 1234);
		config.options().copyDefaults(true);
		
		Game game = new Game(this);
		
		if (config.getBoolean("useBeam")) {
			beam = new BeamAPI();
			if (!config.getBoolean("beam_twofactorrequired")) {
				try {
					beamRobot = new RobotBuilder()
							.username(config.getString("beam_username"))
							.password(config.getString("beam_password"))
							.channel(config.getInt("beam_channelid")).build(beam).get();
					
					startBeamRobot();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (config.getBoolean("beam_twofactorrequired")) {
				getLogger().warning("Two-Factor Auth enabled! Please run /ds auth <auth code> to complete setup of beam robot!");
			}
		}
	}
	
	public void startBeamRobot() {
		if (beamRobot != null) {
			beamRobot.on(Protocol.Report.class, report -> {
                if (report.getTactileCount() > 0) {
                    for (int i = 0; i < report.getTactileList().size();i++) {
                    	TactileInfo button = report.getTactile(i);
                    	switch(button.getId()) {
                    	case 0: //Swap Early
                    		if (game.inGame) swap();
                    		break;
                    	case 1: //Give Lava Bucket
                    		if (game.inGame) giveItem(new ItemStack(Material.LAVA_BUCKET,1));
                    		break;
                    	case 2: //Make Invulnerable
                    		if (game.inGame) givePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,60,6));
                    		break;
                    	case 3: //Instant food and hunger refill
                    		if (game.inGame) {
                    			giveItem(new ItemStack(Material.COOKED_BEEF,64));
                    			givePotionEffect(new PotionEffect(PotionEffectType.SATURATION,1,10));
                    		}
                    		break;
                    	}
                    }
                }
            });
		}
	}
	
	public void updateState(String state) {
        if (beamRobot != null) {
            Protocol.ProgressUpdate.Builder progressBuilder = Protocol.ProgressUpdate.newBuilder();
            progressBuilder.setState(state);

            try {
                if (beamRobot.isOpen()) {
                    beamRobot.write(progressBuilder.build());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	@Override
	public void onDisable() {
		
	}
	
	public void swap() {
		if (game.inGame) {
		Location player1tmp = game.fighters[0].getLocation();
		Location player2tmp = game.fighters[1].getLocation();
		game.fighters[0].teleport(player2tmp);
		game.fighters[1].teleport(player1tmp);
		Bukkit.broadcastMessage("SWAPPED!");
		game.swapTime = 30 + (int)(Math.random() * ((90 - 30) + 1));
		}
	}

	public void giveItem(ItemStack itemToGive) {
		if (game.inGame) {
			if (game.streamingPlayer != null) {
				if (game.fighters[0] == game.streamingPlayer | game.fighters[1] == game.streamingPlayer) {
					game.streamingPlayer.getInventory().addItem(itemToGive);
					game.streamingPlayer.chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
				}
				else {
					game.fighters[0].getInventory().addItem(itemToGive);
					game.fighters[0].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
					game.fighters[1].getInventory().addItem(itemToGive);
					game.fighters[1].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
				}
			} else {
				game.fighters[0].getInventory().addItem(itemToGive);
				game.fighters[0].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
				game.fighters[1].getInventory().addItem(itemToGive);
				game.fighters[1].chat("You have been gifted " + itemToGive.getAmount() + " " + itemToGive.toString() + " by the stream!");
			}
		}
	}

	public void givePotionEffect(PotionEffect effectToGive) {
		if (game.inGame) {
			if (game.streamingPlayer != null) {
				if (game.fighters[0] == game.streamingPlayer | game.fighters[1] == game.streamingPlayer) {
					game.streamingPlayer.addPotionEffect(effectToGive);
					game.streamingPlayer.chat("You have been gifted " + effectToGive.toString() + " by the stream!");
				}
				else {
					game.fighters[0].addPotionEffect(effectToGive);
					game.fighters[0].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
					game.fighters[1].addPotionEffect(effectToGive);
					game.fighters[1].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
				}
			} else {
				game.fighters[0].addPotionEffect(effectToGive);
				game.fighters[0].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
				game.fighters[1].addPotionEffect(effectToGive);
				game.fighters[1].chat("You have been gifted " + effectToGive.toString() + " by the stream!");
			}
		}
	}
}
