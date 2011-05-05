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

	private final WeakHashMap<Vehicle, VehicleData> vehicles = new WeakHashMap<Vehicle, VehicleData>();

	private final int maxLifetime;
	private final boolean strikeLightning;
	private final boolean returnToOwner;

	public VehicleZapper(int maxLifetime, boolean strikeLightning, boolean returnToOwner) {
		this.maxLifetime = maxLifetime;
		this.strikeLightning = strikeLightning;
		this.returnToOwner = returnToOwner;
	}

	/**
	 * Whether this type of vehicle is managed by this zapper instance.
	 * 
	 * @param vehicle
	 *        the vehicle to check
	 * @return
	 */
	protected abstract boolean isManagedVehicle(final Vehicle vehicle);

	/**
	 * Returns the {@link Material} to give to the owner on vehicle destroyal.
	 * 
	 * @param vehicle
	 *        the vehicle that gets destroyed.
	 * @return the Material to add to the owner's inventory.
	 */
	protected abstract Material getVehicleMaterial(final Vehicle vehicle);

	/**
	 * Add a vehicle to be managed by this zapper given it is of the correct type. The time of last usage is set to the current timestamp.
	 * 
	 * @param vehicle
	 *        the vehicle to add
	 */
	public void addVehicle(final Vehicle vehicle) {
		if (isManagedVehicle(vehicle)) {
			final VehicleData data = getVehicleData(vehicle);
			data.updateLastTimeUsed();
			if (returnToOwner)
				data.setOwner(findOwner(vehicle));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.event.vehicle.VehicleListener#onVehicleCreate(org.bukkit.event.vehicle.VehicleCreateEvent)
	 */
	@Override
	public void onVehicleCreate(VehicleCreateEvent event) {
		addVehicle(event.getVehicle());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.event.vehicle.VehicleListener#onVehicleExit(org.bukkit.event.vehicle.VehicleExitEvent)
	 */
	@Override
	public void onVehicleExit(VehicleExitEvent event) {
		final Vehicle vehicle = event.getVehicle();
		if (isManagedVehicle(vehicle))
			getVehicleData(vehicle).updateLastTimeUsed();
	}

	/**
	 * Returns a data container to store vehicle specific data.
	 * 
	 * @param vehicle
	 *        the vehicle to store data for
	 * @return the data container for the given vehicle
	 */
	private VehicleData getVehicleData(final Vehicle vehicle) {
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
	private Player findOwner(final Vehicle vehicle) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.event.vehicle.VehicleListener#onVehicleDestroy(org.bukkit.event.vehicle.VehicleDestroyEvent)
	 */
	@Override
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		final Vehicle vehicle = event.getVehicle();
		if (isManagedVehicle(vehicle))
			vehicles.remove(vehicle);
	}

	/**
	 * This method is called by the Bukkit scheduler to check for abandoned vehicles at regular intervals.
	 */
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
}
