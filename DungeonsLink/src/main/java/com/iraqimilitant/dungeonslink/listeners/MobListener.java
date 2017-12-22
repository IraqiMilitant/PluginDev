package com.iraqimilitant.dungeonslink.listeners;


import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.iraqimilitant.dungeonslink.Constants;
import com.iraqimilitant.dungeonslink.DungeonPerks;
import com.iraqimilitant.dungeonslink.DungeonsLink;
import com.iraqimilitant.dungeonslink.Raider;

import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
/**
 * Handles all event listening required for the mob spawning and control part of the plugin
 * 
 * @author IraqiMilitant
 *
 */
public class MobListener implements Listener {

	/**
	 * tracks entity damage to modify mob attack values to be inline with the set modifiers
	 * for that mob
	 * 
	 * Shawn trolling feature is implemented here as well
	 * 
	 * @param event
	 */
	@EventHandler
	public void entityDamage(EntityDamageByEntityEvent event) {

		if((event.getDamager() instanceof Player)){
			Raider r=Raider.getRaider((Player)event.getDamager());
			if(r instanceof Raider){
				r.setLastHit(event.getEntity().getEntityId());
				if(r.getPerk().equals(DungeonPerks.NOTARGET)){
					if(event.getEntity() instanceof Creature){
						Creature cr=(Creature)event.getEntity();
						cr.setTarget((LivingEntity)event.getDamager());
					}
				}
			}
		}


	}

	@EventHandler
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			Raider r=Raider.getRaider((Player)event.getEntity());
			if(r instanceof Raider){
				r.notifyDamaged(event);
			}
		}


	}

	@EventHandler
	public void entityTarget(EntityTargetEvent event) {
		if(event.getTarget() instanceof Player){
			Raider r=Raider.getRaider((Player)(event.getTarget()));
			if(r instanceof Raider){
				r.notifyTargetted(event);
			}
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event) {

	}



	/**
	 * schedules a task to extinguish a player (setFireTicks can be picky about when it is run)
	 * 
	 * @param p
	 */
	private void extinguish(Player p){
		final Player player=p;
		Bukkit.getServer().getScheduler().runTask(DungeonsLink.getPlugin(DungeonsLink.class), new Runnable(){
			public void run(){
				player.setFireTicks(0);
			}

		});
	}
}
