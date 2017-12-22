package com.iraqimilitant.dungeonslink;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum DungeonPerks {
	NONE(0),
	SPEED(100),
	JUMP(150),
	SATURATION(200),
	NIGHT_VISION(250),
	JUGGERNAUT(300),
	DMG_RESIST(350),
	REGEN(400),
	NOTARGET(500);
	
	public int rating;
	
	private DungeonPerks(int rating){
		this.rating=rating;
	}
	
	public String getName(){
		switch(this){
		case NONE:
			break;
		case SPEED:
			return "Speed Boost";
			
		case JUMP:
			
			return "Jump Boost";
		case SATURATION:
			return "Saturation";
		case NIGHT_VISION:
			return "Night Vision";
		case JUGGERNAUT:
			return "Juggernaut";
		case DMG_RESIST:
			
			return "Damage Resist";
		case REGEN:
			return "Regeneration";
		case NOTARGET:
			return "No Target";
		}
		return "No Perk";
	}
	
	public void clearEffects(Player p){
		switch(this){
		case NONE:
			break;
		case SPEED:
			if(p.hasPotionEffect(PotionEffectType.SPEED)){
				p.removePotionEffect(PotionEffectType.SPEED);
			}
			break;
		case JUMP:
			if(p.hasPotionEffect(PotionEffectType.JUMP)){
				p.removePotionEffect(PotionEffectType.JUMP);
			}
			break;
		case SATURATION:
			if(p.hasPotionEffect(PotionEffectType.SATURATION)){
				p.removePotionEffect(PotionEffectType.SATURATION);
			}
		case NIGHT_VISION:
			if(p.hasPotionEffect(PotionEffectType.NIGHT_VISION)){
				p.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
			break;
		case JUGGERNAUT:
			if(p.hasPotionEffect(PotionEffectType.SLOW)){
				p.removePotionEffect(PotionEffectType.SLOW);
			}
			if(p.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)){
				p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			}
			if(p.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)){
				p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
			}
			break;
		case DMG_RESIST:
			break;
		case REGEN:
			if(p.hasPotionEffect(PotionEffectType.REGENERATION)){
				p.removePotionEffect(PotionEffectType.REGENERATION);
			}
			break;
		case NOTARGET:
			break;
			
		}
	}
	
	public void applyEffect(Player p){
		switch(this){
		case NONE:
			break;
		case SPEED:
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,1800000,2));
			break;
		case JUMP:
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,1800000,2));
			break;
		case NIGHT_VISION:
			p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,1800000,1));
			break;
		case SATURATION:
			p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,1800000,1));
			break;
		case JUGGERNAUT:
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,1800000,2));
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,1800000,4));
			p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,1800000,4));
			break;
		case DMG_RESIST:
			break;
		case REGEN:
			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,1800000,2));
			break;
		case NOTARGET:
			break;
			
		}
	}
	
	public void applyEffect(Player p,EntityDamageEvent event){
		switch(this){
		case NONE:
			break;
		case SPEED:
			break;
		case JUMP:
			break;
		case NIGHT_VISION:
			break;
		case SATURATION:
		case JUGGERNAUT:
			break;
		case DMG_RESIST:
			event.setDamage(event.getDamage()*0.9);
			break;
		case REGEN:
			break;
		case NOTARGET:
			break;
		}
	}
	
	public void applyEffect(Player p,EntityTargetEvent event){
		switch(this){
		case NONE:
			break;
		case SPEED:
			break;
		case JUMP:
			break;
		case NIGHT_VISION:
			break;
		case SATURATION:
		case JUGGERNAUT:
			break;
		case DMG_RESIST:
			break;
		case REGEN:
			break;
		case NOTARGET:
			if(Raider.getRaider(p).getLastHit()!=event.getEntity().getEntityId()){
				event.setCancelled(true);
			}
			break;
			
		}
	}
}
