package io.flixion.infected;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.bimmr.mcinfected.McInfected;

import net.md_5.bungee.api.ChatColor;

public class PL extends JavaPlugin {
	private static McInfected mcInfectedPL;
	private static PL instance;
	
	public static McInfected getMcInfected() {
		return mcInfectedPL;
	}
	
	public static PL getPL() {
		return instance;
	}
	
	public void onEnable() {
		
		instance = this;
		
		if (Bukkit.getPluginManager().getPlugin("McInfected") != null) {
			mcInfectedPL = (McInfected) Bukkit.getPluginManager().getPlugin("McInfected");
			Infection infectionHandler = new Infection();
			getCommand("infected").setExecutor(infectionHandler);
			Bukkit.getPluginManager().registerEvents(infectionHandler, this);
		} else {
			getLogger().log(Level.SEVERE, "Cannot find dependancy [McInfected] - Disbling");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
	
	public static String cc (String m) {
		return ChatColor.translateAlternateColorCodes('&', m);
	}
}
