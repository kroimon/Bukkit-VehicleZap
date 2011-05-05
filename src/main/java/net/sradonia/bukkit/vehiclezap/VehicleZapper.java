package net.sradonia.bukkit.vehiclezap;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public abstract class VehicleZapper extends VehicleListener implements Runnable {

	private WeakHashMap<Vehicle, VehicleData> vehicles = new WeakHashMap<Vehicle, VehicleData>();

	private int maxLifetime;
	private boolean strikeLightning;
	private boolean returnToOwner;

	public VehicleZapper(int maxLifetime, boolean strikeLightning, boolean returnToOwner) {
		this.maxLifetime = maxLifetime;
		this.strikeLightning = strikeLightning;
		this.returnToOwner = returnToOwner;
	}

	protected abstract boolean isManagedVehicle(Vehicle vehicle);

	public void addVehicle(final Vehicle vehicle) {
		if (isManagedVehicle(vehicle)) {
			final VehicleData data = getVehicleData(vehicle);
			data.updateLastTimeUsed();
			if (returnToOwner)
				data.setOwner(findOwner(vehicle));
		}
	}

	@Override
	public void onVehicleCreate(VehicleCreateEvent event) {
		addVehicle(event.getVehicle());
	}

	@Override
	public void onVehicleExit(VehicleExitEvent event) {
		final Vehicle vehicle = event.getVehicle();
		if (isManagedVehicle(vehicle))
			getVehicleData(vehicle).updateLastTimeUsed();
	}

	private VehicleData getVehicleData(Vehicle vehicle) {
		VehicleData data = vehicles.get(vehicle);
		if (data == null) {
			data = new VehicleData();
			vehicles.put(vehicle, data);
		}
		return data;
	}

	/**
	 * Tries to find the owner of the given vehicle.
	 */
	private Player findOwner(Vehicle vehicle) {
		final Vector vehicleVector = vehicle.getLocation().toVector();
		double minDistance = Double.MAX_VALUE;
		Player owner = null;
		for (Player player : vehicle.getWorld().getPlayers()) {
			double distance = player.getLocation().toVector().distance(vehicleVector);
			if (distance < minDistance) {
				minDistance = distance;
				owner = player;
			}
		}
		return owner;
	}

	@Override
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		final Vehicle vehicle = event.getVehicle();
		if (isManagedVehicle(vehicle))
			vehicles.remove(vehicle);
	}

	public void run() {
		final Iterator<Entry<Vehicle, VehicleData>> it = vehicles.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Vehicle, VehicleData> entry = it.next();
			final Vehicle vehicle = entry.getKey();
			final VehicleData data = entry.getValue();
			if (vehicle.isDead()) {
				it.remove();
			} else if (vehicle.isEmpty() && (data.getUnusedTime() >= maxLifetime * 1000)) {
				if (returnToOwner) {
					final Player owner = data.getOwner();
					if (owner != null)
						owner.getInventory().addItem(new ItemStack(getVehicleMaterial(vehicle), 1));
				}
				if (strikeLightning)
					vehicle.getWorld().strikeLightning(vehicle.getLocation());
				vehicle.remove();
				it.remove();
			}
		}
	}

	protected abstract Material getVehicleMaterial(Vehicle vehicle);

}
