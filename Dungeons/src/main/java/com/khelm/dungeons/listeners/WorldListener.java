package com.khelm.dungeons.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Raider;

import org.bukkit.event.block.BlockIgniteEvent;

public class WorldListener implements Listener{
	
	/**
	 * clears potion effects if moving out of the dungeon world
	 * @param event
	 */
	@EventHandler
	public void PlayerChangedWorldEvent(PlayerChangedWorldEvent event){
		if(event.getFrom().equals(Bukkit.getWorld(Constants.dunegonW))){
			//Raider.getRaider(event.getPlayer()).updateArmour(); need to schedule for a few ticks later
			for(PotionEffect pe:event.getPlayer().getActivePotionEffects()){
				event.getPlayer().removePotionEffect(pe.getType());
			}
		}
	}
	
	/**
	 * blocks all block ignition in the dungeon world (prevents nether rooms with nether
	 * mobs from slowly being covered with fire due to blazes)
	 * 
	 * @param event
	 */
	@EventHandler
	public void BlockIgniteEvent(BlockIgniteEvent event){
		if(event.getBlock().getLocation().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			event.setCancelled(true);
		}
	}
}
