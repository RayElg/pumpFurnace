package pumpFurnace;

import java.io.Serializable;

import org.bukkit.Location;

public class LocationSerializable implements Serializable {
	
	private static final long serialVersionUID = 4749491257645610628L;
	String worldName;
	double x;
	double y;
	double z;
	
	public LocationSerializable(Location l) {
		this.x = l.getX(); this.y = l.getY(); this.z = l.getZ();
		this.worldName = l.getWorld().getName();
	}
	
	public Location toLocation(Main p) throws NullPointerException{ //Will return a location equivalent to the one used to construct this, save orientation (yaw etc.)
		return(new Location(p.getServer().getWorld(this.worldName),x,y,z));
	}
	
}
