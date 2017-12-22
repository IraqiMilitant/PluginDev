package com.khelm.dungeons.mobcontrol;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.io.Serializable;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * enum which implements my custom moba abilities
 * 
 * @author iraqimilitant
 *
 */
public enum MobAbilities implements Serializable {
	LEECH,
	SPLIT,
	RANGERESIST,
	THORNS,
	MASSIVE;
	
	/**
	 * applies any ability effects needed on mob spawn
	 * 
	 * @param m
	 */
	public void reactSpawn(Mob m){
		switch (this){
		case LEECH:
			break;
		case SPLIT:
			break;
		case RANGERESIST:
			break;
		case THORNS:
			break;
		case MASSIVE:
			m.getEntity().setMaxHealth(m.getEntity().getMaxHealth()*3);
			m.getEntity().setHealth(m.getEntity().getMaxHealth());	
			m.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW,90000,1));
			
			break;
			
		}
	}
	
	/**
	 * applies ability effects needed when the Mob is damaged
	 * 
	 * @param e
	 */
	public void reactDamage(EntityDamageByEntityEvent e){
		switch (this){
		case LEECH:
			
			break;
		case SPLIT:
			break;
		case RANGERESIST:
			if(!(e.getDamager() instanceof LivingEntity)){
				e.setDamage(e.getDamage()/2);
			}
			break;
		case THORNS:
			if(e.getDamager() instanceof LivingEntity){
				((LivingEntity)e.getDamager()).setHealth((((LivingEntity)e.getDamager()).getHealth()-(e.getDamage()*0.1))<0?0:(((LivingEntity)e.getDamager()).getHealth()-(e.getDamage()*0.1)));
			}
			break;
		case MASSIVE:
			break;
			
		}
	}
	
	/**
	 * applies ability effects needed when the mob attacks a player
	 * 
	 * @param e
	 */
	public void reactAttack(EntityDamageByEntityEvent e){
		switch (this){
		case LEECH:
			((LivingEntity)e.getDamager()).setMaxHealth(((LivingEntity)e.getDamager()).getMaxHealth()+e.getDamage());
			((LivingEntity)e.getDamager()).setHealth(((LivingEntity)e.getDamager()).getHealth()+e.getDamage());
			break;
		case SPLIT:
			break;
		case RANGERESIST:
			break;
		case THORNS:
			break;
		case MASSIVE:
			break;
			
		}
	}

	/**
	 * applies any ability effects needed on death
	 * 
	 * @param e
	 */
	public void reactDeath(EntityDeathEvent e){
		switch (this){
		case LEECH:
			break;
		case SPLIT:
			//TODO: fill this in
			break;
		case RANGERESIST:
			break;
		case THORNS:
			break;
		case MASSIVE:
			break;
		}
	}
}
