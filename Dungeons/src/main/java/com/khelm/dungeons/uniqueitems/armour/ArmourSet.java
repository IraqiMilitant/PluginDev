package com.khelm.dungeons.uniqueitems.armour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;

public enum ArmourSet {
	NONE,
	BATMAN,
	IRAQI,
	STEVO,
	SHAWN,
	ALICHEEK,
	MADJOCK;

	ArmourSet(){

	}

	public void ApplyEffects(Player p,int count){
		switch(this){
		case NONE:
			break;
		case BATMAN:
			switch(count){
			case 4:
			case 3:
				p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,1000000,100000));
				Raider.getRaider(p).registerArmourEffect(PotionEffectType.FIRE_RESISTANCE);
			case 2:
				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,1000000,3));
				Raider.getRaider(p).registerArmourEffect(PotionEffectType.JUMP);
			case 1:
			}
			break;
		case IRAQI:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case STEVO:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case SHAWN:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case ALICHEEK:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case MADJOCK:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		}

	}

	public void ApplyEffects(PlayerMoveEvent e,int count){
		switch(this){
		case NONE:
			break;
		case BATMAN:
			switch(count){
			case 4:
				ArmourEffect.FIRETRAIL.applyEffect(e.getFrom());
			case 3:
			case 2:
			case 1:
			}
			break;
		case IRAQI:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case STEVO:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case SHAWN:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case ALICHEEK:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		case MADJOCK:
			switch(count){
			case 4:
			case 3:
			case 2:
			case 1:
			}
			break;
		}
	}
	
	public static ItemStack getItem(String name){
		Random r=new Random();
		int slot=r.nextInt(4);
		ItemStack result=null;
		switch (ArmourSet.valueOf(name.toUpperCase())){
		case NONE:
			return null;
		case BATMAN:
			LeatherArmorMeta meta=null;
			switch(slot){
			case 3:
				result=new ItemStack(Material.LEATHER_BOOTS,1);
				meta=((LeatherArmorMeta)result.getItemMeta());
				meta.setDisplayName( "Boots of Batman");
				break;
			case 2:
				result=new ItemStack(Material.LEATHER_LEGGINGS,1);
				meta=((LeatherArmorMeta)result.getItemMeta());
				meta.setDisplayName( "Tights of Batman");
				break;
			case 1:
				result=new ItemStack(Material.LEATHER_CHESTPLATE,1);
				meta=((LeatherArmorMeta)result.getItemMeta());
				meta.setDisplayName( "Suit of Batman");
				break;
			case 0:
				result=new ItemStack(Material.LEATHER_HELMET,1);
				meta=((LeatherArmorMeta)result.getItemMeta());
				meta.setDisplayName( "Mask of Batman");
				break;
			}
			
			meta.setColor(Color.BLACK);
			ArrayList<String> lore=new ArrayList<String>();
			lore.add("Batman Armour Set");
			lore.add("4 pieces: Firetrail");
			lore.add("3 pieces: Fireproof");
			lore.add("2 pieces: Jump Boost");
			meta.setLore(lore);
			result.setItemMeta(meta);
			break;
		case IRAQI:
			break;
		case STEVO:
			break;
		case SHAWN:
			break;
		case ALICHEEK:
			break;
		case MADJOCK:
			break;
			
		}
		return result;
	}

}
