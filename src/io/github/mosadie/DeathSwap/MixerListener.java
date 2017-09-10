package io.github.mosadie.DeathSwap;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.mosadie.MixBukkit.events.ControlMouseDownInput;

public class MixerListener  implements Listener {
	private DeathSwap ds;
	public MixerListener(DeathSwap ds) {
		this.ds = ds;
	}
	
	public static final String SWAP_EARLY = "Swap Early";
	public static final String GIVE_LAVA = "Give Lava";
	public static final String DMG_RST = "Damage Resistance";
	public static final String GIVE_FOOD = "Give Food";
	public static final String SPAWN_MOBS = "Spawn Mobs";
	
	@EventHandler
	public void OnButtonDown(ControlMouseDownInput event) {
		switch(event.getControlID()) {
		case SWAP_EARLY:
			if (ds.inGame) ds.swap(true);
			break;
		case GIVE_LAVA:
			if (ds.inGame) ds.giveItem(new ItemStack(Material.LAVA_BUCKET,1), "lava bucket");
			break;
		case DMG_RST:
			if (ds.inGame) ds.givePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,120,6), "invulnerability for 6 seconds");
			break;
		case GIVE_FOOD:
			if (ds.inGame) {
				ds.giveItem(new ItemStack(Material.COOKED_BEEF,64), "cooked beef");
				ds.givePotionEffect(new PotionEffect(PotionEffectType.SATURATION,1,20),"the pleasent feeling of not being hungry");
			}
			break;
		case SPAWN_MOBS:
			if (ds.inGame) {
				if (ds.isStreamerInGame()) {
					ds.spawnMob(ds.streamingPlayer);
				} else {
					ds.spawnMob();
				}
			}
		}
	}
}
