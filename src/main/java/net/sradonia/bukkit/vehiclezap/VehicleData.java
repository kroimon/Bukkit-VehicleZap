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

	/**
	 * Updates the last-used timestamp to now.
	 */
	public void updateLastTimeUsed() {
		lastTimeUsed = System.currentTimeMillis();
	}

	/**
	 * @return the number of milliseconds passed since the last call to {@link #updateLastTimeUsed()}.
	 */
	public long getUnusedTime() {
		return System.currentTimeMillis() - lastTimeUsed;
	}

	/**
	 * @param player
	 *        the owner of the vehicle
	 */
	public void setOwner(Player player) {
		ownerName = player.getName();
	}

	/**
	 * @return the owner of the vehicle or <code>null</code> if none has been set or the player is disconnected
	 */
	public Player getOwner() {
		return Bukkit.getServer().getPlayer(ownerName);
	}
}
