package com.khelm.dungeons.mobcontrol;

import java.io.Serializable;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * enum controls all group level abilities
 * 
 * @author iraqimilitant
 *
 */
public enum MobGroupAbilities implements Serializable{
	LIFELINK,
	POWERLINK;//I know bad names, life link encourages singular focus, powerlink the opposite

	/**
	 * applies any effects needed when the group is spawned
	 * 
	 * @param mg
	 */
	public void reactSpawn(MobGroup mg){

		switch (this){
		case LIFELINK:
			break;
		case POWERLINK:
			break;

		}
	}

	/**
	 * applies any effects needed when a member of the group is damaged
	 * @param e
	 * @param mg
	 */
	public void reactDamage(EntityDamageByEntityEvent e,MobGroup mg){
		double targetHealth=0;
		switch (this){
		case LIFELINK:

			for(Mob m:mg.getMobs()){

				if(!m.getEntity().equals(e.getEntity())&&m.getEntity().isValid()){
					targetHealth=m.getEntity().getHealth()+e.getDamage();
					if(targetHealth>m.getEntity().getMaxHealth()){
						m.getEntity().setHealth(m.getEntity().getMaxHealth());
					}else{
						m.getEntity().setHealth(targetHealth);
					}
				}
			}
			break;
		case POWERLINK:


			break;
		}
	}

	/**
	 * applies any effects needed when a member of the group damages a player
	 * 
	 * @param e
	 * @param mg
	 */
	public void reactAttack(EntityDamageByEntityEvent e,MobGroup mg){
		switch (this){
		case LIFELINK:
			break;
		case POWERLINK:
			break;
		}
	}

	/**
	 * Apply ability effects needed on death of a group member
	 * 
	 * @param e
	 * @param mg
	 */
	public void reactDeath(EntityDeathEvent e,MobGroup mg){
		double targetHealth=0;
		switch (this){
		case LIFELINK:
			break;
		case POWERLINK:
			for(Mob m:mg.getMobs()){

				if(!m.getEntity().equals(e.getEntity())&&m.getEntity().isValid()){
					targetHealth=m.getEntity().getHealth()*2;
					if(targetHealth>m.getEntity().getMaxHealth()){
						m.getEntity().setMaxHealth(targetHealth);;
					}
					m.getEntity().setHealth(targetHealth);
					
				}
			}
			break;
		}
	}
}
