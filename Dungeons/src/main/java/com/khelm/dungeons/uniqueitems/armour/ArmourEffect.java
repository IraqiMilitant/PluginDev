package com.khelm.dungeons.uniqueitems.armour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.khelm.dungeons.Constants;

public enum ArmourEffect {
	FIRETRAIL,
	EXPLOSIVE,
	SUICIDAL,
	POTATO;
	
	ArmourEffect(){
		
	}
	
	public void applyEffect(Player p){
		switch(this){
			case FIRETRAIL:
				
				break;
			case EXPLOSIVE:
				
				break;
			case SUICIDAL:
				
				break;
			case POTATO:

				
				break;
		}
	}
	
	public void applyEffect(Location l){
		switch(this){
			case FIRETRAIL:
				l.getBlock().setType(Material.FIRE);
				break;
			case EXPLOSIVE:
				
				break;
			case SUICIDAL:
				
				break;
			case POTATO:

				
				
				break;
		}
	}
	
	public void applyEffect(Location l,Player p){
		switch(this){
			case FIRETRAIL:
				l.getBlock().setType(Material.FIRE);
				break;
			case EXPLOSIVE:
				
				break;
			case SUICIDAL:
				
				break;
			case POTATO:
				ItemStack i=new ItemStack(Material.POISONOUS_POTATO);
				ItemMeta meta=i.getItemMeta();
				meta.setDisplayName("Revival Potato:"+p.getDisplayName());
				Entity e=Bukkit.getWorld(Constants.dunegonW).dropItem(l,i);
				
				
				break;
		}
	}

}
