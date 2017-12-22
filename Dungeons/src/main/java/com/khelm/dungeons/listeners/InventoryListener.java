package com.khelm.dungeons.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;
import com.khelm.dungeons.cmdexecutors.GearCommandExecutor;

public class InventoryListener implements Listener {

	/**
	 * inventory close triggers armour updates, and loot rolls (if inventory is a loot roll inv)
	 *
	 * @param event
	 */
	@EventHandler
	public void InventoryCloseEvent(InventoryCloseEvent event){
		if(((Player)event.getPlayer())instanceof Player){
			if(((Player)event.getPlayer()).getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
				Player p=(Player)event.getPlayer();
				Raider.getRaider(p).updateArmour();
				if(GearCommandExecutor.getLootRolls().containsKey(event.getInventory())){
					GearCommandExecutor.removeInv(event.getInventory());
					int loot=Raider.getRaider(p).assessInventory(event.getInventory());
					if(loot>0){
						final int l=loot;
						final Player pl=p;
						Bukkit.getServer().getScheduler().runTask(Dungeons.getPlugin(Dungeons.class), new Runnable(){
							public void run(){
								Raider.getRaider(pl).generateLoot((int)(l*0.8),true);
							}
							
						});
						
					}else{
						final Inventory inv=event.getInventory();
						final Player fp=p;
						Bukkit.getServer().getScheduler().runTask(Dungeons.getPlugin(Dungeons.class), new Runnable(){
							public void run(){
								fp.openInventory(inv);
							}
							
						});
						
					}


				}

			}
		}


	}
	
	@EventHandler
	public void craftEvent(CraftItemEvent event){
		
		if(event.getRecipe().getResult().getType().equals(Material.WOOD_SWORD)&&event.getWhoClicked().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			event.setCancelled(true);
		}
	}

}
