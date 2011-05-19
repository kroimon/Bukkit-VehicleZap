package net.sradonia.bukkit.vehiclezap;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.config.Configuration;

public class VehicleZapPlugin extends JavaPlugin {
	private static final Logger log = Logger.getLogger("Minecraft");

	private VehicleZapper boatZapper;
	private VehicleZapper minecartZapper;

	public void onEnable() {
		// Load configuration
		final Configuration config = getConfiguration();
		if (!new File(getDataFolder(), "config.yml").exists()) {
			config.setProperty("checkInterval", 30);
			config.setProperty("boats.enable", true);
			config.setProperty("boats.maxLifetime", 120);
			config.setProperty("boats.strikeLightning", true);
			config.setProperty("boats.returnToOwner", false);
			config.setProperty("minecarts.enable", false);
			config.setProperty("minecarts.maxLifetime", 120);
			config.setProperty("minecarts.strikeLightning", true);
			config.setProperty("minecarts.returnToOwner", false);
			config.save();
		}

		// Register listeners and schedule checks
		final PluginManager pluginManager = getServer().getPluginManager();
		final BukkitScheduler scheduler = getServer().getScheduler();
		final int checkInterval = config.getInt("checkInterval", 30) * 20;

		if (config.getBoolean("boats.enable", true)) {
			boatZapper = new BoatZapper(
					config.getStringList("boats.worlds", null),
					config.getInt("boats.maxLifetime", 120),
					config.getBoolean("boats.strikeLightning", true),
					config.getBoolean("boats.returnToOwner", false));
			pluginManager.registerEvent(Type.VEHICLE_CREATE, boatZapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_EXIT, boatZapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_DESTROY, boatZapper, Priority.Monitor, this);
			scheduler.scheduleSyncRepeatingTask(this, boatZapper, checkInterval, checkInterval);
		}

		if (config.getBoolean("minecarts.enable", false)) {
			minecartZapper = new MinecartZapper(
					config.getStringList("boats.worlds", null),
					config.getInt("minecarts.maxLifetime", 120),
					config.getBoolean("minecarts.strikeLightning", true),
					config.getBoolean("minecarts.returnToOwner", false));
			pluginManager.registerEvent(Type.VEHICLE_CREATE, minecartZapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_EXIT, minecartZapper, Priority.Monitor, this);
			pluginManager.registerEvent(Type.VEHICLE_DESTROY, minecartZapper, Priority.Monitor, this);
			scheduler.scheduleSyncRepeatingTask(this, minecartZapper, checkInterval, checkInterval);
		}

		// Load and manage existing vehicles
		for (World world : getServer().getWorlds())
			loadWorldVehicles(world);
		pluginManager.registerEvent(Type.WORLD_LOAD, new WorldListener() {
			@Override
			public void onWorldLoad(WorldLoadEvent event) {
				loadWorldVehicles(event.getWorld());
			}
		}, Priority.Monitor, this);

		// Log success
		log.info(getDescription().getFullName() + " enabled.");
	}

	/**
	 * Loads all vehicles from the given world and adds them to the specific zappers.
	 * @param world the world to load vehicles from
	 */
	private void loadWorldVehicles(final World world) {
		final List<Entity> entities = world.getEntities();
		if (boatZapper != null && boatZapper.isEnabledForWorld(world))
			for (Entity entity : entities)
				if (entity instanceof Vehicle)
					boatZapper.addVehicle((Vehicle) entity);
		if (minecartZapper != null && minecartZapper.isEnabledForWorld(world))
			for (Entity entity : entities)
				if (entity instanceof Vehicle)
					minecartZapper.addVehicle((Vehicle) entity);
	}

	public void onDisable() {
		log.info(getDescription().getFullName() + " disabled.");
	}
}
