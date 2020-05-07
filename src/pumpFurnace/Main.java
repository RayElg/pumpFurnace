package pumpFurnace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	HashMap<Location, PumpFurnace> pumpFurnaces;

	int range = 2;
	
	// Methods called when plugin is enabled/disabled
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		pumpFurnaces = loadPFs(); //Load in the pumpFurnaces
		
		pumpFurnaces.forEach((k,v) -> {v.reInit(this); v.attachMetadata();});  //Reinitialize & attach metadata to all of the pumpFurnace blocks, so we can tell that it is a pumpFurnace
		pumpFurnaces.entrySet().removeIf(entries -> entries.getValue().f == null); //If we didn't successfully reinit furnace, remove.
	}

	@Override
	public void onDisable() {
		savePFs(); // Save all the PFs for next bootup
	}

	// Event handler, calls whenever an item is smelted in a furnace
	@EventHandler(ignoreCancelled = true)
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {
		Furnace f = (Furnace) event.getBlock().getState();

		if (f.getBurnTime() >= 1000) { // Check if the furnace has less than 1000 ticks of burntime
			return; // Exit if the furnace already has enough fuel
		} else { // If this furnace has less than one lava bucket's worth of fuel,
			if (f.hasMetadata("pFObject")) { // If this is a pumpfurnace
				Location loc = f.getLocation();
				PumpFurnace pF = pumpFurnaces.get(loc); // Get pF
				if (pF.extractLava()) { // Try to extract lava from surroundings
					short burnTime = f.getBurnTime();
					f.setBurnTime((short) (201 + burnTime)); // Add 50 ticks to the burntime
					Bukkit.broadcastMessage("Added 50 ticks");
					f.update();
				} else {
					return; // If no lava can be extracted, exit
				}
			}
		}

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("pump")) { // On /pump
			Block b = ((Player) sender).getTargetBlock(null, 20);
			if (b.getType() == Material.FURNACE && !b.hasMetadata("pFObject")) { // If player is looking at a non-pump
																					// furnace
				PumpFurnace pF = new PumpFurnace((Furnace) b.getState(), this, range);
				pumpFurnaces.put(b.getLocation(), pF); // Add this pumpfurnace to the
																							// list
				pF.attachMetadata();
				sender.sendMessage("Success! This furnace is now a pump furnace.");
				return true;
			}
		}
		return false;
	}

	public void savePFs() { // Saves all pumpFurnaces for persistence
		try {
			File dF = this.getDataFolder(); // pumpFurnace data plugin
			if (!dF.exists())
				dF.mkdir();

			File f = new File(dF, "/pumpFurnaces.txt"); // pF file
			if (!f.exists())
				f.createNewFile();

			HashMap<LocationSerializable,PumpFurnace> forOut = new HashMap<LocationSerializable,PumpFurnace>(); //Version of the hashmap to save
			
			pumpFurnaces.forEach((K,V) -> {forOut.put(new LocationSerializable(K),V);}); //Convert pumpFurnaces to serializable version
			
			
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f)); // Write the hashMap
			out.writeObject(forOut);
			out.close();

		} catch (IOException e) {
			getLogger().log(Level.WARNING, "IOException while saving pump furnace locations");
			e.printStackTrace();
		}
	}

	public HashMap<Location, PumpFurnace> loadPFs() { // Loads pFs from file
		try {
			File dF = this.getDataFolder(); // pumpFurnace data plugin
			if (!dF.exists())
				dF.mkdir();

			File f = new File(dF, "/pumpFurnaces.txt"); // Locations file
			if(!f.exists())
				f.createNewFile();
			
			
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));

			HashMap<LocationSerializable, PumpFurnace> fromFile = (HashMap<LocationSerializable, PumpFurnace>) in.readObject(); //Load hashmap w/ serializable version of location from file
			in.close();
			
			HashMap<Location,PumpFurnace> hMap = new HashMap<Location,PumpFurnace>();
			
			fromFile.forEach((K,V) -> {hMap.put(K.toLocation(this),V);}); //Convert to hashmap we'll use
			
			return hMap;

		} catch (IOException e) {
			getLogger().log(Level.WARNING, "IOException while loading pump furnace locations");
			e.printStackTrace();
			return new HashMap<Location, PumpFurnace>();
		} catch (ClassNotFoundException e) {
			getLogger().log(Level.WARNING, "Missing class while loading pump furnace locations");
			e.printStackTrace();
			return new HashMap<Location, PumpFurnace>();
		} catch (NullPointerException e) {
			getLogger().log(Level.WARNING, "Missing world while loading pump furnace locations");
			e.printStackTrace();
			return new HashMap<Location, PumpFurnace>();
		}

	}

}
