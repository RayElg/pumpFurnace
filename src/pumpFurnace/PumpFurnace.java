package pumpFurnace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.Levelled;
import org.bukkit.metadata.FixedMetadataValue;

public class PumpFurnace implements Serializable{
	private static final long serialVersionUID = -7408688351604753824L;
	transient Furnace f; //Furnace block this belongs to
	transient Location l; //Location of the furnace block
	transient ArrayList<Location> fuelBlocks; //What blocks to check for lava
	transient Main p; //Plugin
	LocationSerializable lS; //Serializable location of the furnace block. l and f can be derived from this.
	int r; //Range of this pumpFurnace
	
	public PumpFurnace(Furnace F, Main plugin, int R){
		this.r = R;
		this.p = plugin;
		this.f = F;
		this.l = f.getLocation();
		this.fuelBlocks = genList();
		this.lS = new LocationSerializable(l);
	}

	public void attachMetadata() {
		f.setMetadata("pFObject", new FixedMetadataValue(p, this)); //Save metadata: saves the pumpFurnace object with key pFObject
	}
	
	public ArrayList<Location> genList(){ //Generates all the possible blocks the pumpFurnace should check for
		ArrayList<Location> list = new ArrayList<Location>();
		World w = l.getWorld();
		int x,y,z;
		x = l.getBlockX(); y = l.getBlockY(); z = l.getBlockZ();
		y--; //Get block below furnace
		x -= r; z -= r; //Corner of range
		
		for(int e = 0; e > -(2*r) + 1; e--) {//Y
			for(int i = 0; i < (2*r) + 1; i++) { //X
				for(int j = 0; j < (2*r) + 1; j++) { //Z
					list.add(new Location(w, x + i, y + e, z+j));
				}
			}
		}
		
		return list;
	}

	
	public boolean extractLava() {
		
		if(fuelBlocks.size() == 0) { //If there's no more fuel blocks...
			this.fuelBlocks = this.genList(); //Regenerate the fuel blocks, since another lava block could have been placed here
		}
		
		ListIterator<Location> iterator = fuelBlocks.listIterator();
		
		while(iterator.hasNext()){
			Block b = iterator.next().getBlock();
			if(b.getType() == Material.LAVA) { //If this block is lava source
				Levelled fluid = (Levelled) b.getBlockData();
				if(fluid.getLevel() == 0) { //If fluid level is 1 (sourceblock)
					b.setType(Material.COBBLESTONE);
					iterator.remove(); //Remove this from list: already harvested. This will speed things up.
					return true;
				}
	    	}
	    	iterator.remove(); //Remove from list: not a valid fuel block.
		}
		

		return false;
	}
	
	public void reInit(Main plugin) throws NullPointerException{ //Reinitialize f and l from lS
		this.p = plugin;
		this.l = new Location(p.getServer().getWorld(lS.worldName),lS.x,lS.y,lS.z);
		Block b = l.getBlock();
		if(b.getType() == Material.FURNACE) {
			this.f = (Furnace) b.getState();
		}
		this.fuelBlocks = genList();
	}
	
}


