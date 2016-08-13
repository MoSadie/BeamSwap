package io.github.mosadie.DeathSwap;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pro.beam.api.BeamAPI;
import pro.beam.interactive.net.packet.Protocol;
import pro.beam.interactive.net.packet.Protocol.Report.TactileInfo;
import pro.beam.interactive.robot.Robot;
import pro.beam.interactive.robot.RobotBuilder;

public class DeathSwap extends JavaPlugin {
	public FileConfiguration config = this.getConfig();
	public BeamAPI beam;
	public Robot beamRobot;
	public boolean isGameRunning = false;
	@Override
	public void onEnable() {
		config.addDefault("useBeam", false);
		config.addDefault("beam_username", "Username");
		config.addDefault("beam_password", "Password");
		config.addDefault("beam_twofactorrequired", false);
		config.addDefault("beam_channelid", 1234);
		config.options().copyDefaults(true);
		
		if (config.getBoolean("useBeam")) {
			beam = new BeamAPI();
			if (!config.getBoolean("beam_twofactorrequired")) {
				try {
					beamRobot = new RobotBuilder()
							.username(config.getString("beam_username"))
							.password(config.getString("beam_password"))
							.channel(config.getInt("beam_channelid")).build(beam).get();
					
					beamRobot.on(Protocol.Report.class, report -> {
		                if (report.getTactileCount() > 0) {
		                    for (int i = 0; i < report.getTactileList().size();i++) {
		                    	TactileInfo button = report.getTactile(i);
		                    	switch(button.getId()) {
		                    	case 0: //Swap Early
		                    		break;
		                    	case 1: //Give Lava Bucket
		                    		break;
		                    	case 2: //Make Invulnerable
		                    		break;
		                    	case 3: //Instant food and hunger refill
		                    		break;
		                    	}
		                    }
		                }
		            });
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (config.getBoolean("beam_twofactorrequired")) {
				getLogger().warning("Two-Factor Auth enabled! Please run /dsauth <auth code> to complete setup of beam robot!");
			}
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
}
