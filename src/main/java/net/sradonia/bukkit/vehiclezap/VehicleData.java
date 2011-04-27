package net.sradonia.bukkit.vehiclezap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VehicleData {
	private long lastTimeUsed;

	/**
	 * The name of the player who (most likely) created this vehicle.
	 * Only store the name in case of dis-/reconnect.
	 */
	private String ownerName;

	public void updateLastTimeUsed() {
		lastTimeUsed = System.currentTimeMillis();
	}

	public long getUnusedTime() {
		return System.currentTimeMillis() - lastTimeUsed;
	}

	public void setOwner(Player player) {
		ownerName = player.getName();
	}

	public Player getOwner() {
		return Bukkit.getServer().getPlayer(ownerName);
	}
}
