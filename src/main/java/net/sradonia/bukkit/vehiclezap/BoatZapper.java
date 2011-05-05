package net.sradonia.bukkit.vehiclezap;

import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;

public class BoatZapper extends VehicleZapper {

	public BoatZapper(int maxLifetime, boolean strikeLightning, boolean returnToOwner) {
		super(maxLifetime, strikeLightning, returnToOwner);
	}

	@Override
	protected boolean isManagedVehicle(final Vehicle vehicle) {
		return (vehicle instanceof Boat);
	}

	@Override
	protected Material getVehicleMaterial(final Vehicle vehicle) {
		return Material.BOAT;
	}

}
