package io.github.mosadie.DeathSwap;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.mosadie.BeamBukkit.ReportEvent;
import pro.beam.interactive.net.packet.Protocol.Report;
import pro.beam.interactive.net.packet.Protocol.Report.TactileInfo;

public class ReportListener implements Listener {
	private DeathSwap ds;
	public ReportListener(DeathSwap ds) {
		this.ds = ds;
	}
	
	@EventHandler
	public void onReport(ReportEvent re) {
		Report report = re.getReport(); 
		if (report.getTactileCount() > 0) {
			for (int i = 0; i < report.getTactileList().size();i++) {
				TactileInfo button = report.getTactile(i);
				if (button.getPressFrequency() >= 1) {
					switch(button.getId()) {
					case 0: //Swap Early
						if (ds.inGame) ds.swap();
						break;
					case 1: //Give Lava Bucket
						if (ds.inGame) ds.giveItem(new ItemStack(Material.LAVA_BUCKET,1));
						break;
					case 2: //Make Invulnerable
						if (ds.inGame) ds.givePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,120,6));
						break;
					case 3: //Instant food and hunger refill
						if (ds.inGame) {
							ds.giveItem(new ItemStack(Material.COOKED_BEEF,64));
							ds.givePotionEffect(new PotionEffect(PotionEffectType.SATURATION,1,20));
						}
						break;
					}
				}
			}
		}
	}

}
