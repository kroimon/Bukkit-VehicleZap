package net.sradonia.bukkit.vehiclezap;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.config.Configuration;

public class VehicleZapPlugin extends JavaPlugin {
	private static final Logger log = Logger.getLogger("Minecraft");

	public void onEnable() {
		// Load configuration
		final Configuration config = getConfiguration();
		if (!new File(getDataFolder(), "config.yml").exists()) {
			config.setProperty("checkInterval", 30);
			config.setProperty("boats.enable", true);
			config.setProperty("boats.maxLifetime", 120);
			config.setProperty("boats.strikeLightning", true);
			config.setProperty("minecarts.enable", false);
			config.setProperty("minecarts.maxLifetime", 120);
			config.setProperty("minecarts.strikeLightning", true);
			config.save();
		}

		// Register listeners and schedule checks
		final PluginManager pluginManager = getServer().getPluginManager();
		final BukkitScheduler scheduler = getServer().getScheduler();
		final int checkInterval = config.getInt("checkInterval", 30) * 20;

		if (config.getBoolean("boats.enable", true)) {
			final VehicleZapper zapper = new VehicleZapper(Boat.class,
					config.getInt("boats.maxLifetime", 120),
					config.getBoolean("boats.strikeLightning", true));
			pluginManager.registerEvent(Type.VEHICLE_CREATE, zapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_EXIT, zapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_DESTROY, zapper, Priority.Monitor, this);
			scheduler.scheduleSyncRepeatingTask(this, zapper, checkInterval, checkInterval);
		}

		if (config.getBoolean("minecarts.enable", false)) {
			final VehicleZapper zapper = new VehicleZapper(Minecart.class,
					config.getInt("minecarts.maxLifetime", 120),
					config.getBoolean("minecarts.strikeLightning", true));
			pluginManager.registerEvent(Type.VEHICLE_CREATE, zapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_EXIT, zapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_DESTROY, zapper, Priority.Monitor, this);
			scheduler.scheduleSyncRepeatingTask(this, zapper, checkInterval, checkInterval);
		}

		final PluginDescriptionFile pdf = getDescription();
		log.info(pdf.getFullName() + " enabled.");
	}

	public void onDisable() {
		final PluginDescriptionFile pdf = getDescription();
		log.info(pdf.getFullName() + " disabled.");
	}
}
