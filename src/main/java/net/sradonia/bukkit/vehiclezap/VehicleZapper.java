package net.sradonia.bukkit.vehiclezap;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleListener;

public class VehicleZapper extends VehicleListener implements Runnable {

	private WeakHashMap<Vehicle, Long> vehicles = new WeakHashMap<Vehicle, Long>();
	private Class<? extends Vehicle> vehicleClass;

	private int maxLifetime;
	private boolean strikeLightning;

	public VehicleZapper(Class<? extends Vehicle> vehicleClass, int maxLifetime, boolean strikeLightning) {
		this.vehicleClass = vehicleClass;
		this.maxLifetime = maxLifetime;
		this.strikeLightning = strikeLightning;
	}

	@Override
	public void onVehicleCreate(VehicleCreateEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicleClass.isAssignableFrom(vehicle.getClass()))
			updateVehicle(vehicle);
	}

	@Override
	public void onVehicleExit(VehicleExitEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicleClass.isAssignableFrom(vehicle.getClass()))
			updateVehicle(vehicle);
	}

	private void updateVehicle(Vehicle vehicle) {
		vehicles.put(vehicle, System.currentTimeMillis());
	}

	@Override
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicleClass.isAssignableFrom(vehicle.getClass()))
			vehicles.remove(vehicle);
	}

	public void run() {
		long now = System.currentTimeMillis();

		Iterator<Entry<Vehicle, Long>> it = vehicles.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Vehicle, Long> entry = it.next();
			Vehicle vehicle = entry.getKey();
			if (vehicle.isDead()) {
				it.remove();
			} else if (vehicle.isEmpty() && (now - entry.getValue() > maxLifetime * 1000)) {
				if (strikeLightning)
					vehicle.getWorld().strikeLightning(vehicle.getLocation());
				vehicle.remove();
				it.remove();
			}
		}
	}
}
