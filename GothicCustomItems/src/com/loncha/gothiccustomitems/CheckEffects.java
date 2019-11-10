package com.loncha.gothiccustomitems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CheckEffects implements Listener {
		
	ArrayList<String> efectosItems = new ArrayList<String>();
	
	ArrayList<String> listaEfectos = new ArrayList<String>(Arrays.asList("ABSORPTION","BLINDNESS","CONFUSION","DAMAGE_RESISTANCE","FAST_DIGGING","FIRE_RESISTANCE","GLOWING","HARM","HEAL","HEALTH_BOOST","HUNGER","INCREASE_DAMAGE","INVISIBILITY","JUMP","LEVITATION","LUCK","NIGHT_VISION","POISON","REGENERATION","SATURATION","SLOW","SLOW_DIGGING","SPEED","UNLUCK","WATER_BREATHING","WEAKNESS","WITHER")); 
	ArrayList<String> listaAcciones = new ArrayList<String>(Arrays.asList("eat","attack","equip","mainhand","inventory"));  
	
	ArrayList<String> items = new ArrayList<String>();
	ArrayList<String> efectos = new ArrayList<String>();
	ArrayList<String> valores = new ArrayList<String>();
	ArrayList<String> amplificadores = new ArrayList<String>();
	ArrayList<String> acciones = new ArrayList<String>();
	
	ArrayList<String> contadorEfectos = new ArrayList<String>();
	ArrayList<String> contadorAcciones = new ArrayList<String>();
	
	ArrayList<PotionEffectType> efectosActivosMainHand = new ArrayList<PotionEffectType>();
	String itemActivado;
	
	public CheckEffects() {
		loadDatos();							
	}
	
	@EventHandler
	public void onItemEat(PlayerItemConsumeEvent e) {
		Player p = e.getPlayer();
		
		ItemStack item = e.getItem();
		String nombreItem;
		
		if (item.getItemMeta().getDisplayName() == null) {
			nombreItem = item.getType().toString();
		} else {
			nombreItem = item.getItemMeta().getDisplayName();
		}
		
		cleanArrays(nombreItem, "eat", p);	
		
	}
	
	//Evento que controla cuando una entidad ha atacado para ver si tiene que establecer un efecto de poción
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		//Comprueba si la el evento ha sido desencadenado por una entidad (el daño puede ser desencadenado por muchas cosas, hace falta asegurarse de que es una entidad viva)
		if (e instanceof LivingEntity) {
			ItemStack item = new ItemStack(Material.AIR);
			LivingEntity d = (LivingEntity) e.getEntity();
			
			Player p = (Player) e.getDamager();
			
			try {
				//Comprueba si el damager es un player.
				if (e.getDamager() instanceof Player) {
						
					item = p.getInventory().getItemInMainHand();
					String nombreItem;
					
					//Comprueba si el item que tienes en la mano tiene un nombre custom o no
					if (item.getItemMeta().getDisplayName() == null) {
						nombreItem = item.getType().toString();
					} else {
						nombreItem = item.getItemMeta().getDisplayName();
					}
				
					cleanArrays(nombreItem,"attack",d);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	//Evento que controla cuando cambias de el item que tienes en la mano principal
	@EventHandler
	public void onHandChange(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		ItemStack item = new ItemStack(Material.AIR);
		item = p.getInventory().getItem(e.getNewSlot()); 
		
		String nombreItem;
		
		//Comprueba si tienes un item en la mano
		if (item != null) {
			if (item.getItemMeta().getDisplayName() == null) {
				nombreItem = item.getType().toString();
				
				if (nombreItem != itemActivado) {
					for (PotionEffect effect : p.getActivePotionEffects()) {
						for (PotionEffectType active : efectosActivosMainHand) {
							if (effect.getType() == active) {
								p.removePotionEffect(active);
							}
						}	
					}
					efectosActivosMainHand = new ArrayList<PotionEffectType>();
				}
			} else {
				//Comprueba si has cambiado el item en la mano por otro diferente y te quita los efectos de poción
				nombreItem = item.getItemMeta().getDisplayName();
				
				if (nombreItem != itemActivado) {
					for (PotionEffect effect : p.getActivePotionEffects()) {
						for (PotionEffectType active : efectosActivosMainHand) {
							if (effect.getType() == active) {
								p.removePotionEffect(active);
							}
						}	
					}
					efectosActivosMainHand = new ArrayList<PotionEffectType>();
				}
			}
			
			cleanArrays(nombreItem,"mainhand",p);
			
		} else {
			//Si no tienes un item te quita todos los efectos de poción que formen parte de los "efectos de item en mainhand"
			for (PotionEffect effect : p.getActivePotionEffects()) {
				for (PotionEffectType active : efectosActivosMainHand) {
					if (effect.getType() == active) {
						p.removePotionEffect(active);
					}
				}	
			}
			efectosActivosMainHand = new ArrayList<PotionEffectType>();
		}
		
		
		
	}
		
	public void loadDatos() {
		try {
			File f = new File("plugins/GothicCustomItems/efectosItems.txt");
			System.out.println("cargando");
			efectosItems = new ArrayList<String>();
			
			if (f.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String readLine;

				while ((readLine = br.readLine()) != null) {
					efectosItems.add(readLine);
				}
				
				br.close();
			} else {
				f.createNewFile();
			}
			
			contarStrings();
			
		}catch(Exception k) {
			k.printStackTrace();
		}
	}
	
	public void contarStrings() {
		ArrayList<String> effectLine = new ArrayList<String>();
		efectos = new ArrayList<String>();
		acciones = new ArrayList<String>();
		valores = new ArrayList<String>();
		amplificadores = new ArrayList<String>();
		
		String nItem = "";
		
		for (String s : efectosItems) {
			StringTokenizer tokens = new StringTokenizer(s, " ");
			while (tokens.hasMoreTokens()) {
				effectLine.add(tokens.nextToken());
			}

			int contEfectos = 0, contAcciones = 0;
			for (String n : effectLine) {
				if (listaEfectos.contains(n)) {
					contEfectos++;
					efectos.add(n);
				} else if (listaAcciones.contains(n)) {
					contAcciones++;
					acciones.add(n);
				} else if (isNumeric(n)) {
					valores.add(n);
				} else if (n.startsWith("amp.")) {
					char[] temp = n.toCharArray();
					for (char c : temp) {
						String temps = String.valueOf(c);
						if (isNumeric(temps)) {
							amplificadores.add(temps);
						}
					}
				} else {				
					nItem+=n+" ";
				}
			}
			nItem = nItem.substring(0, nItem.length()-1);
			items.add(nItem);
			nItem = "";
			
			effectLine = new ArrayList<String>();
			contadorEfectos.add(Integer.toString(contEfectos));
			contadorAcciones.add(Integer.toString(contAcciones));
		}
	}
	
	public void cleanArrays(String nombreItem, String accion, LivingEntity p) {
		try {
			if (items.contains(nombreItem)) {
				itemActivado = nombreItem;
				
				ArrayList<String> copiaEfectos = new ArrayList<String>(efectos);
				ArrayList<String> copiaValores = new ArrayList<String>(valores);
				ArrayList<String> copiaAcciones = new ArrayList<String>(acciones);
				ArrayList<String> copiaAmplificadores = new ArrayList<String>(amplificadores);
				
				int nEfectos = 0, nAcciones = 0;
				
				//Bucles para limpiar los arraylist y dejar todos los string en el mismo índice
				for (int i = items.indexOf(nombreItem)-1; i >= 0; i--) {
					nEfectos+= Integer.parseInt(contadorEfectos.get(i));
					nAcciones+= Integer.parseInt(contadorAcciones.get(i));
				}
							
				for (int k = 0; k < nEfectos; k++) {
					copiaEfectos.remove(0);
					copiaValores.remove(0);
				}
				
				for (int k = 0; k < nAcciones; k++) {
					copiaAcciones.remove(0);
				}
				
				//Comprueba si el objeto tiene el argumento para aplicar efectos cuando haga falta
				
				for (int i = 0; i < copiaAcciones.size(); i++) {
					if (copiaAcciones.get(i).equalsIgnoreCase(accion)) {
						for (int k = 0; k < Integer.parseInt(contadorEfectos.get(items.indexOf(nombreItem))); k++ ) {
							
							try {
								if (copiaAmplificadores.size() > 0) {
									p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(copiaEfectos.get(k)),Integer.parseInt(copiaValores.get(k)),Integer.parseInt(copiaAmplificadores.get(k))-1));
								} else {
									p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(copiaEfectos.get(k)),Integer.parseInt(copiaValores.get(k)),1));
								}
								
								if (accion.equalsIgnoreCase("mainhand")) {
									efectosActivosMainHand.add(PotionEffectType.getByName(copiaEfectos.get(k)));
								}
								
								System.out.println("Has aplicado el efecto: "+ copiaEfectos.get(k));
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public static boolean isNumeric(String strNum) {
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException | NullPointerException nfe) {
	        return false;
	    }
	    return true;
	}
}
