package net.sradonia.bukkit.vehiclezap;

import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Vehicle;

public class MinecartZapper extends VehicleZapper {

	public MinecartZapper(int maxLifetime, boolean strikeLightning, boolean returnToOwner) {
		super( maxLifetime, strikeLightning, returnToOwner);
	}

	@Override
	protected boolean isManagedVehicle(Vehicle vehicle) {
		return (vehicle instanceof Minecart) && !(vehicle instanceof StorageMinecart || vehicle instanceof PoweredMinecart);
	}

	@Override
	protected Material getVehicleMaterial(Vehicle vehicle) {
		return Material.MINECART;
	}

}
