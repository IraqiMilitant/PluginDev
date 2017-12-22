package com.khelm.dungeons.listeners;


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
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.DungeonPerks;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;
import com.khelm.dungeons.mobcontrol.Mob;

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
		Mob m;
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
		
		if((event.getEntity() instanceof Player)&&(event.getDamager() instanceof LivingEntity)){//melee
			m=Mob.dungeonMob((LivingEntity)event.getDamager());
			if(m instanceof Mob){ 
				event.setDamage(event.getDamage()*m.getAtk());
				m.notifyAttack(event);
			}

		}else if((event.getEntity() instanceof Player)&&(event.getDamager() instanceof Projectile)){//ranged
			Projectile p=(Projectile)event.getDamager();
			m=Mob.dungeonMob((LivingEntity)p.getShooter());
			if(m instanceof Mob){
				event.setDamage(event.getDamage()*m.getAtk());
				//proj.remove(p);
			}
		}else if(Mob.dungeonMob(event.getEntity()) instanceof Mob){
			if(event.getDamager() instanceof Player){
				if(Raider.getRaider((Player)event.getDamager()).getInvading()){
					event.setCancelled(true);
				}else{
					Mob.dungeonMob(event.getEntity()).notifyDamaged(event);//tell the mob it was hurt (for use with abilities)
				}
			}else{
				Mob.dungeonMob(event.getEntity()).notifyDamaged(event);//tell the mob it was hurt (for use with abilities)
			}

		}

//		if(event.getEntity() instanceof Player){
//			Raider r=Raider.getRaider((Player)event.getEntity());
//			if(!r.isInDungeon()){
//				if((((Player)event.getEntity()).getUniqueId().toString().equals("3f7b38dd-bd83-4661-b349-52545113986a"))&& Constants.trollshawn){
//					event.setDamage(1000.0);//if its shawn hit him with a big stick
//				}
//			}
//		}
		


		if(!(event.getEntity() instanceof Player)||event.isCancelled()){
			return;
		}

		Player p=(Player)event.getEntity();

		Raider r=Raider.getRaider(p);
		if(p.getLocation().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))&&!r.isInDungeon()&&!r.getInvading()&&!r.getArbiter()){
			event.setCancelled(true);
		}
		if(p.getWorld().equals(Bukkit.getWorld(Constants.dunegonW))&&(((p.getHealth() - event.getDamage() < 1))||(p.getFireTicks()>0&&p.getHealth()-event.getDamage()<=1))){
			event.setCancelled(true);
			if(r.isInDungeon()){
				p.teleport(Constants.res);
			}else{
				p.teleport(Constants.exit);
			}
			p.setHealth(p.getMaxHealth());
			extinguish(p);
			for(PotionEffect pe:r.getPlayer().getActivePotionEffects()){
				Dungeons.getPlugin(Dungeons.class).log(r.getName());
				r.getPlayer().removePotionEffect(pe.getType());
			}
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);


			if(!r.getDead()){
				r.setDead(true);
				if(r.hasGroup()){
					r.getGroup().notifyDeath((Player)event.getEntity());
				}
			}

		}else if((p.getHealth() - event.getDamage() < 1)&&r.getInvading()){
			event.setCancelled(true);
			//p.playEffect(EntityEffect.DEATH);
			p.teleport(Constants.exit);
			p.setHealth(p.getMaxHealth());
			extinguish(p);
			for(PotionEffect pe:r.getPlayer().getActivePotionEffects()){
				Dungeons.getPlugin(Dungeons.class).log(r.getName());
				r.getPlayer().removePotionEffect(pe.getType());
			}
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			p.sendMessage(ChatColor.RED+"YOU DIED!!, Invasion Failed");
			if(!r.getDead()){
				r.setDead(true);
				if(r.hasGroup()){
					r.getGroup().notifyDeath((Player)event.getEntity());
				}

			}
		}
	


	}
	
	@EventHandler
	public void EntityDamageEvent(EntityDamageEvent event){
		
		
		
		if(!(event.getEntity() instanceof Player)||event.isCancelled()){
			return;
		}
		
		if(event.getEntity() instanceof Player){
			Raider r=Raider.getRaider((Player)event.getEntity());
			if(r instanceof Raider){
				r.notifyDamaged(event);
			}
		}

		Player p=(Player)event.getEntity();

		Raider r=Raider.getRaider(p);
		if(p.getLocation().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))&&!r.isInDungeon()&&!r.getInvading()&&!r.getArbiter()){
			event.setCancelled(true);
		}
		if(p.getWorld().equals(Bukkit.getWorld(Constants.dunegonW))&&(((p.getHealth() - event.getDamage() < 1))||(p.getFireTicks()>0&&p.getHealth()-event.getDamage()<=1))){
			event.setCancelled(true);
			if(r.isInDungeon()){
				p.teleport(Constants.res);
			}else{
				p.teleport(Constants.exit);
			}
			p.setHealth(p.getMaxHealth());
			extinguish(p);
			for(PotionEffect pe:r.getPlayer().getActivePotionEffects()){
				Dungeons.getPlugin(Dungeons.class).log(r.getName());
				r.getPlayer().removePotionEffect(pe.getType());
			}
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);


			if(!r.getDead()){
				r.setDead(true);
				if(r.hasGroup()){
					r.getGroup().notifyDeath((Player)event.getEntity());
				}
			}

		}else if((p.getHealth() - event.getDamage() < 1)&&r.getInvading()){
			event.setCancelled(true);
			//p.playEffect(EntityEffect.DEATH);
			p.teleport(Constants.exit);
			p.setHealth(p.getMaxHealth());
			extinguish(p);
			for(PotionEffect pe:r.getPlayer().getActivePotionEffects()){
				Dungeons.getPlugin(Dungeons.class).log(r.getName());
				r.getPlayer().removePotionEffect(pe.getType());
			}
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			p.sendMessage(ChatColor.RED+"YOU DIED!!, Invasion Failed");
			if(!r.getDead()){
				r.setDead(true);
				if(r.hasGroup()){
					r.getGroup().notifyDeath((Player)event.getEntity());
				}

			}
		}
	}

	@EventHandler
	public void entityTarget(EntityTargetLivingEntityEvent event) {
		if(event.getTarget() instanceof Player){
			Raider r=Raider.getRaider((Player)(event.getTarget()));
			if(r instanceof Raider){
				r.notifyTargetted(event);
			}
		}
		if(Mob.dungeonMob(event.getEntity()) instanceof Mob){
			if(!(event.getTarget() instanceof Player)){
				event.setCancelled(true);
			}else if(event.getTarget() instanceof Player){
				Player p=(Player)event.getTarget();
				if(Raider.getRaider(p).getInvading()){
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event) {
		Random rand=new Random();
		Mob m=Mob.dungeonMob(event.getEntity());
		if(m instanceof Mob){
			m.notifyDead(event);
		}

		if(event.getEntity().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			event.getDrops().clear();
			if(rand.nextInt(100)<30){
				event.getDrops().add(new ItemStack(Material.ARROW,rand.nextInt(5-1)+1));
			}else if(rand.nextInt(100)<30){
				event.getDrops().add(new ItemStack(Material.COOKED_BEEF,rand.nextInt(2)+1));
			}

		}
	}

	@EventHandler
	public void creatureSpawn(CreatureSpawnEvent event) {
		if(event.getLocation().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			if(event.getSpawnReason()!=CreatureSpawnEvent.SpawnReason.CUSTOM){
				event.setCancelled(true);
			}
		}
		//		if(Mob.getEntities().contains(event.getEntity())){
		//			event.setCancelled(true);
		//		}
	}

	@EventHandler
	public void slimeSplitEvent(SlimeSplitEvent event) {//to be tested
		Mob m=Mob.dungeonMob(event.getEntity());
		if(m instanceof Mob){
			double modifier=m.getAtk();
			event.setCount((int)(event.getCount()*modifier*2));
		}
	}
	
	/**
	 * schedules a task to extinguish a player (setFireTicks can be picky about when it is run)
	 * 
	 * @param p
	 */
	private void extinguish(Player p){
		final Player player=p;
		Bukkit.getServer().getScheduler().runTask(Dungeons.getPlugin(Dungeons.class), new Runnable(){
			public void run(){
				player.setFireTicks(0);
			}

		});
	}
}
