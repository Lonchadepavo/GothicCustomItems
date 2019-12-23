package com.loncha.gothiccustomitems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagFloat;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;

public class Main extends JavaPlugin implements Listener{	
	ArrayList<String> efectosItems = new ArrayList<String>();
	ArrayList<String> efectosCargados = new ArrayList<String>();
	CheckEffects ce;
	CustomDrops cd;
	
	public void onEnable() {	
		try {
			cd = new CustomDrops(); //Inicializar la clase de customdrops (objetos custom al romper bloques)
			ce = new CheckEffects(); //Inicializar la clase checkeffects (efectos de pociones sobre objetos)
			
			//Registrar los eventos
			getServer().getPluginManager().registerEvents(this, this);
			getServer().getPluginManager().registerEvents(this.ce, this);
			getServer().getPluginManager().registerEvents(this.cd, this);
			
			//Se crea la carpeta si no existe
			File d = new File("plugins/GothicCustomItems");
			if (!d.exists()) {
				d.mkdir();
			}
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player)sender;
		ArrayList<String> args2 = new ArrayList<String>(Arrays.asList(args));
		// /nuevoitem <item> <cantidad> <durabilidad> <atributos> <valorAtributo> <areaEfectoAtributo>
		if (cmd.getName().equalsIgnoreCase("nuevoitem") && sender.hasPermission("gcustomitems.crearitems")) {
			if (args2.size() < 6 ) {
				p.sendMessage("Uso: /nuevoitem <item> <cantidad> <durabilidad> <atributos> <valorAtributo> <areaEfectoAtributo>");
				
				return true;
				
			} else {
				ItemStack item = new ItemStack(Material.getMaterial(args2.get(0).toUpperCase()), Integer.parseInt(args2.get(1))); //Crear el objeto
				args2.remove(0);
				args2.remove(0);
				
				item.setDurability((short) Integer.parseInt(args2.get(0))); //Establecer la durabilidad del objeto
				args2.remove(0);
				
				net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
				NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
				NBTTagList modifiers = new NBTTagList();
				
				ArrayList<String> atributos = new ArrayList<String>();
				ArrayList<Float> valorAtributo = new ArrayList<Float>();
				
				//Bucles para gestionar los atributos nbt que intentas establecer
				for (int i = 0; i < args2.size(); i++) {
					if (args2.get(i).startsWith("generic")) {
						atributos.add(args2.get(i));
					}	
				}
				
				for (int i = 0; i < args2.size(); i++) {
					if (args2.get(i).startsWith("generic")) {
						args2.remove(i);
					}	
				}
				
				for (int i = 0; i < args2.size(); i++) {
					try {
						float temp;
						temp = Float.parseFloat(args2.get(i));
						valorAtributo.add(temp);
						
					} catch (Exception e) {
						
					}
				}
				
				for (int i = 0; i < args2.size(); i++) {
					try {
						@SuppressWarnings("unused")
						float temp;
						temp = Float.parseFloat(args2.get(i));
						args2.remove(i);
						
					} catch (Exception e) {
						
					}
				}
				
				//Establecer los atributos nbt
				for (int i = 0; i < atributos.size(); i++) {
					NBTTagCompound attribute = new NBTTagCompound();
					
					attribute.set("AttributeName", new NBTTagString(atributos.get(i)));
					attribute.set("Name", new NBTTagString("generic."+atributos.get(i)));
					attribute.set("Amount", new NBTTagFloat(valorAtributo.get(i)));
					attribute.set("Operation", new NBTTagInt(0));
					attribute.set("UUIDLeast", new NBTTagInt(894654));
					attribute.set("UUIDMost", new NBTTagInt(2872));
					
					for (int k = 0; k < args2.size(); k++) {
						attribute.set("Slot", new NBTTagString(args2.get(k)));
					}
					
					modifiers.add(attribute);
				}
				
				compound.set("AttributeModifiers", modifiers);
				
				nmsStack.setTag(compound);
				
				item = CraftItemStack.asBukkitCopy(nmsStack);
				
				p.getInventory().addItem(item);
				p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 10, 29);
				
				return true;
			}
			
		} else if (cmd.getName().equalsIgnoreCase("setnombre") && sender.hasPermission("gcustomitems.crearitems")) {
			if (p.getInventory().getItemInMainHand() != null) {
				//Comando para cambiar el nombre de un item
				if (args.length < 2) {
					p.sendMessage("Uso: /setnombre <color> <nombre>");
					
					return true;
					
				} else {
					ItemStack item = p.getInventory().getItemInMainHand();
					
					if (item != null) {
						ItemMeta meta = item.getItemMeta();
						
						ChatColor color = ChatColor.valueOf(args[0]);
						
						String nombre = "";
						for (int i = 1; i < args.length; i++) {
							if (i == args.length-1) {
								nombre+=args[i];
							} else {
								nombre+=args[i]+" ";
							}
						}
						
						meta.setDisplayName(color+nombre);	
						item.setItemMeta(meta);
						
						return true;
					} else {
						p.sendMessage(ChatColor.RED+"Tienes que tener un item en la mano");
					}
				}
			}
			
		} else if (cmd.getName().equalsIgnoreCase("setdescripcion") && sender.hasPermission("gcustomitems.crearitems")) {
			if (p.getInventory().getItemInMainHand() != null) {
				//Comando para cambiar la descripción de un item
				if (args.length < 2) {
					p.sendMessage("Uso: /setdescripcion <color> <descripcion>");
					
					return true;
					
				} else {
					ItemStack item = p.getInventory().getItemInMainHand();
					
					if (item != null) {
						ItemMeta meta = item.getItemMeta();
						
						ChatColor color = ChatColor.valueOf(args[0]);
						
						List<String> descripcion = new ArrayList<String>();
						String desc = "";
						for(int i = 1; i < args.length; i++) {
							desc+=args[i]+" ";
						}
						descripcion.add(color+desc);
						
						meta.setLore(descripcion);	
						item.setItemMeta(meta);
						
						return true;
					} else {
						p.sendMessage(ChatColor.RED+"Tienes que tener un item en la mano");
					}
				}
			}
			
		} else if (cmd.getName().equalsIgnoreCase("setefectos") && sender.hasPermission("gcustomitems.crearitems")) {
			if (p.getInventory().getItemInMainHand() != null) {
				// /setefectos <efectos> <duracion> <accion> <nivelefecto>
				if (args.length < 3) {
					p.sendMessage("Uso: /setefectos <efectos> <duracion> <accion> <nivelefecto>");
					
					return true;
					
				} else {
					if (p.getInventory().getItemInMainHand() != null) {
						String s = "";
						
						if (p.getInventory().getItemInMainHand().getItemMeta().getDisplayName() == null) {
							s += p.getInventory().getItemInMainHand().getType()+" ";
						} else {
							s += p.getInventory().getItemInMainHand().getItemMeta().getDisplayName()+" ";
						}
						
						
						for (String n : args2) {
							s += n+" ";
						}
						
						efectosItems.add(s);
						
						saveDatos(true);
						ce.loadDatos();
						efectosItems = new ArrayList<String>();
						
						p.sendMessage("Nuevo efecto establecido: " + s);
						
						return true;
					} else {
						p.sendMessage(ChatColor.RED+"Tienes que tener un item en la mano");
					}
				}
			}
			
		} else if (cmd.getName().equalsIgnoreCase("verefectos") && sender.hasPermission("gcustomitems.crearitems")) {
			//Ver la lista de efectos establecidos.
			loadDatos();
			if (efectosCargados.size() > 0) {	
				for (String s : efectosCargados) {
					p.sendMessage(s);
				}
			} else {
				p.sendMessage(ChatColor.RED+"No hay efectos guardados");
			}
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("borrarefecto") && sender.hasPermission("gcustomitems.crearitems")) {
			//Borrar un efecto concreto o toda la lista
			if (args.length < 1) {
				p.sendMessage("Uso: /borrarefecto <todos/numeroefecto>");
				
				return true;
			} else {
				loadDatos();
				if (isNumeric(args[0])) {
					if (Integer.parseInt(args[0]) < efectosCargados.size() && Integer.parseInt(args[0]) >= 0 ) {
						p.sendMessage("Has eliminado el efecto: "+ efectosCargados.get(Integer.parseInt(args[0])));
						efectosCargados.remove(Integer.parseInt(args[0]));
						efectosItems = efectosCargados;
						
						File f = new File("plugins/GothicCustomItems/efectosItems.txt");
						f.delete();
						saveDatos(false);
						
						ce.loadDatos();
						ce.contarStrings();
					} else {
						p.sendMessage("El número tiene que ser menor a: "+efectosCargados.size() + " y mayor que -1");
					}
				} else if (args[0].equalsIgnoreCase("todos")){
					efectosCargados = new ArrayList<String>();
					efectosItems = new ArrayList<String>();
					
					File f = new File("plugins/GothicCustomItems/efectosItems.txt");
					f.delete();
					
					ce.loadDatos();
					ce.contarStrings();
					
					p.sendMessage("Has borrado todos los efectos guardados");
					
				} else {
					p.sendMessage("Uso: /borrarefecto <todos/numeroefecto>");
				}
				
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("recargarefectos") && sender.hasPermission("gcustomitems.crearitems")) {
			//Comando para recargar los efectos (por si ha habido algún problema)
			p.sendMessage(ChatColor.YELLOW+"Cargando efectos...");
			ce.loadDatos();
			p.sendMessage(ChatColor.GREEN+"Efectos cargados");
			
			return true;
		} 
 		return false;
	}
	
	//MÉTODO PARA GUARDAR LOS DATOS EN UN .TXT
	public void saveDatos(Boolean accion) {
		//CÓDIGO PARA GUARDAR LA FICHA EN UN ARCHIVO
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("plugins/GothicCustomItems/efectosItems.txt", accion));
			
			for(int i = 0; i < efectosItems.size(); i++) {
				bw.write(efectosItems.get(i)+"\n");
			}
			
			bw.close();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//MÉTODO PARA CARGAR DATOS DEL .TXT
	public void loadDatos() {
		try {
			File f = new File("plugins/GothicCustomItems/efectosItems.txt");
			efectosCargados = new ArrayList<String>();
			
			if (f.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String readLine;

				while ((readLine = br.readLine()) != null) {
					efectosCargados.add(readLine);
				}
				
				br.close();
			} else {
				f.createNewFile();
			}
			
		}catch(Exception k) {
			k.printStackTrace();
		}
	}
	
	//MÉTODO PARA COMPROBAR SI UN STRING ES NUMÉRICO
	@SuppressWarnings("unused")
	public static boolean isNumeric(String strNum) {
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException | NullPointerException nfe) {
	        return false;
	    }
	    return true;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = e.getClickedBlock();
			
			if (b.getType().toString().toUpperCase().contains("SHULKER_BOX")) {
				e.setCancelled(true);
			}
		}
	}
	
}
