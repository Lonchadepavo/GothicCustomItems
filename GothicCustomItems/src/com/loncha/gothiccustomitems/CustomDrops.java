package com.loncha.gothiccustomitems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class CustomDrops implements Listener{
	ArrayList<ItemStack> customItems = new ArrayList<ItemStack>();
	String[][] arrBiomas = null;
	String[][] arrRegiones = null;
	String[][] arrOriginalItems = null;
	String[][] arrColocables = null;
	
	private File file;
	private FileConfiguration customDrops;
	
	public CustomDrops() throws IOException, InvalidConfigurationException {
		setup();
		cargarItems();
	}
	
	//Método para cargar el archivo de configuración personalizado (customdrops.yml)
	public void setup() throws IOException, InvalidConfigurationException {
		file = new File("plugins/GothicCustomItems/customdrops.yml");
		
		if (!file.exists()) {
			file.getParentFile().mkdir();
			file.createNewFile();
		}
		
		customDrops = new YamlConfiguration();
		customDrops.load(file);
	}
	
	//Establece customdrops como archivo de configuración personalizado
	public FileConfiguration getCustomConfig() {
		return this.customDrops;
	}
	
	//Método para leer la información de customdrops.yml
	public void cargarItems() {
		if (getCustomConfig().getConfigurationSection("items") != null) {
			int itemCounter = 0, itemCounter2 = 1; //Contadores de items
			
			arrColocables = new String[getCustomConfig().getConfigurationSection("items").getKeys(false).size()][2];
			arrOriginalItems = new String[getCustomConfig().getConfigurationSection("items").getKeys(false).size()][2];
			
			//Recorre el archivo de configuración, cada subitem que hay en la sección "items"
			for (String s : getCustomConfig().getConfigurationSection("items").getKeys(false)) {
				itemCounter2 = 1;
				
				//Se crea el itemstack con la información del fichero de configuración
				ItemStack is = new ItemStack(Material.getMaterial(getCustomConfig().getString("items."+s+".new-item")), Integer.parseInt(getCustomConfig().getString("items."+s+".amount")));
				ItemMeta im = is.getItemMeta();
			  
				//Se establece el lore del itemstack
				im.setDisplayName(ChatColor.valueOf(getCustomConfig().getString("items."+s+".color"))+getCustomConfig().getString("items."+s+".name"));
				im.setLore(getCustomConfig().getStringList("items."+s+".description"));
				is.setItemMeta(im);
			  
				//Se inicializan los arrays
				if (arrBiomas == null) {
					arrBiomas = new String[getCustomConfig().getConfigurationSection("items").getKeys(false).size()][getCustomConfig().getStringList("items."+s+".biomes").size()+1];
				}
				
				if (arrRegiones == null) {
					arrRegiones = new String[getCustomConfig().getConfigurationSection("items").getKeys(false).size()][getCustomConfig().getStringList("items."+s+".regions").size()+1];
				}
				
				//Se guarda la id del item en los arrays
				arrBiomas[itemCounter][0] = getCustomConfig().getString("items."+s+".id");
				arrRegiones[itemCounter][0] = getCustomConfig().getString("items."+s+".id");
				arrColocables[itemCounter][0] = getCustomConfig().getString("items."+s+".id");
				
				//Se recorren los arrays de biomas y regiones para guardar en ellos los biomas/regiones del fichero
				for (String s2 : getCustomConfig().getStringList("items."+s+".biomes")) {
					arrBiomas[itemCounter][itemCounter2] = s2;			  
					itemCounter2++;  
				}
			  
				itemCounter2 = 1;		  
				for (String s2 : getCustomConfig().getStringList("items."+s+".regions")) {
					arrRegiones[itemCounter][itemCounter2] = s2;
					itemCounter2++;  
				}
				
				//Se guarda el item original y su id
				arrOriginalItems[itemCounter][0] = getCustomConfig().getString("items."+s+".id");
				arrOriginalItems[itemCounter][1] = getCustomConfig().getString("items."+s+".original-item");
				
				//Establece si es colocable o no (solo en bloques)
				arrColocables[itemCounter][1] = getCustomConfig().getString("items."+s+".colocable");
			  
				itemCounter++;
				customItems.add(is); //Se añade el nuevoitemstack al arraylist de items
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		
		//Bucles para comprobar si el bloque que has roto está en la lista de items originales, después comprueba si estás en uno de los biomas/regiones que tiene asociados y te devuelve el item correspondiente.
		for (int i = 0; i < arrOriginalItems.length; i++) {
			if (b.getType() == Material.getMaterial(arrOriginalItems[i][1])) {
				for (int j = 0 ; j < arrBiomas.length; j++) {
					for (int l = 1; l < arrBiomas[j].length; l++) {
						
						if (arrOriginalItems[i][0].equals(arrBiomas[j][0])) {
							if (p.getLocation().getBlock().getBiome().equals(Biome.valueOf(arrBiomas[j][l]))) {
								
								b.setType(Material.AIR);
								b.getWorld().dropItem(b.getLocation(), customItems.get(Integer.parseInt(arrOriginalItems[i][0])));
							}
							
						}
					}
				}
				
				for (int j = 0 ; j < arrRegiones.length; j++) {
					for (int l = 1; l < arrRegiones[j].length; l++) {						
						if (arrOriginalItems[i][0].equals(arrRegiones[j][0])) {
							if (inRegion(p.getLocation(),arrRegiones[j][l])) {
								
								b.setType(Material.AIR);
								b.getWorld().dropItem(b.getLocation(), customItems.get(Integer.parseInt(arrOriginalItems[i][0])));
							}
						}
					}
				}
				
			}
			
		}
	}
		
	public static boolean inRegion(Location loc, String region) {

	    Vector v = BukkitUtil.toVector(loc);
	    RegionManager manager = WGBukkit.getRegionManager(loc.getWorld());
	    ApplicableRegionSet set = manager.getApplicableRegions(v);
	    
	    if (manager.hasRegion(region)) {
	    	
	    	return true;
	    } else {
	    	return false;
	    }

	}
	
}
