package io.flixion.infected;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.bimmr.mcinfected.McInfected;
import com.bimmr.mcinfected.Events.McInfectedGameEndEvent;
import com.bimmr.mcinfected.Events.McInfectedPlayerDeathEvent;
import com.bimmr.mcinfected.Events.McInfectedPlayerInfectEvent;
import com.bimmr.mcinfected.Events.McInfectedPlayerLeaveEvent;
import com.bimmr.mcinfected.IPlayers.IPlayer;
import com.bimmr.mcinfected.IPlayers.IPlayer.Team;
import com.bimmr.mcinfected.Listeners.DamageInfo.DamageType;

public class Infection implements Listener, CommandExecutor {
	private Random rng = new Random();
	private HashMap<UUID, BukkitTask> inTransition = new HashMap<>();
	
	@EventHandler (ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player target = (Player) e.getEntity();
			IPlayer iTarget = McInfected.getLobbyManager().getIPlayer(target);
			if (iTarget.getTeam() == Team.Human) {
				if (rng.nextInt(101) <= 2) { //2% infection chance
					initInTransitionTask(target);
				}
			}
		}
	}
	
	@EventHandler
	public void onQuit(McInfectedPlayerLeaveEvent e) {
		if (inTransition.containsKey(e.getPlayer().getUniqueId())) {
			curePlayer(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onDeath(McInfectedPlayerDeathEvent e) {
		if (inTransition.containsKey(e.getKilled().getPlayer().getUniqueId())) {
			curePlayer(e.getKilled().getPlayer());
			e.setCancelled(true);
			e.getKilled().infect(DamageType.Other);
		}
	}
	
	@EventHandler
	public void detectInfect(McInfectedPlayerInfectEvent e) {
		if (inTransition.containsKey(e.getIPlayer().getPlayer().getUniqueId())) {
			curePlayer(e.getIPlayer().getPlayer());
		}
	}
	
	@EventHandler
	public void onEndGame(McInfectedGameEndEvent e) {
		for (Map.Entry<UUID, BukkitTask> entry : inTransition.entrySet()) {
			curePlayer(Bukkit.getPlayer(entry.getKey()));
		}
	}
	
	private void initInTransitionTask(Player p) {
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, true, true), true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, true, true), true);
		BukkitTask t = Bukkit.getScheduler().runTaskTimer(PL.getPL(), new Runnable() {
			
			@Override
			public void run() {
				p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10 * 20, 2, true, true), true);
				p.damage(4);
				p.sendMessage(PL.cc("&a&oYou have been bitten... Use a syringe to stop the infection..."));
			}
		}, 0, 15 * 20);
		inTransition.put(p.getUniqueId(), t);
	}
	
	private void curePlayer(Player p) {
		p.removePotionEffect(PotionEffectType.CONFUSION);
		p.removePotionEffect(PotionEffectType.SLOW);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
		p.sendMessage(PL.cc("&a&oYou have been cured!"));
		inTransition.get(p.getUniqueId()).cancel();
		inTransition.remove(p.getUniqueId());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("infected")) {
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("cure")) {
					if (Bukkit.getPlayer(args[1]) != null) {
						if (inTransition.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
							curePlayer(Bukkit.getPlayer(args[1]));
						} else {
							sender.sendMessage("This player is not infected!");
						}
					} else {
						sender.sendMessage("Player is not online!");
					}
				} else if (args[0].equalsIgnoreCase("infect")) {
					if (Bukkit.getPlayer(args[1]) != null) {
						if (!inTransition.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
							initInTransitionTask(Bukkit.getPlayer(args[1]));
						} else {
							sender.sendMessage("This player is already infected!");
						}
					} else {
						sender.sendMessage("Player is not online!");
					}
				} else {
					sender.sendMessage("/infected (cure|infect) <name>");	
				}
			} else {
				sender.sendMessage("/infected (cure|infect) <name>");
			}
		}
		return true;
	}
}
