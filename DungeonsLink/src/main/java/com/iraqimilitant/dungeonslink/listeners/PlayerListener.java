package com.iraqimilitant.dungeonslink.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.iraqimilitant.dungeonslink.Constants;
import com.iraqimilitant.dungeonslink.DungeonsLink;
import com.iraqimilitant.dungeonslink.Raider;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


/**
 * Handles player interact events
 * 
 * @author IraqiMilitant
 *
 */
public class PlayerListener implements Listener {



	/**
	 * reacts to entity damage, to block/adjust it as needed and to trigger
	 * the 'respawn'/'death' behaviour for players in dungeons, and block all damage in 
	 * dungeon world when not in dungeon or invading.
	 * 
	 * @param event
	 */
//	@EventHandler(priority=EventPriority.HIGH)
//	public void entityDamageEvent(EntityDamageEvent event){
//
//		if(!(event.getEntity() instanceof Player)){
//			return;
//		}
//
//		Player p=(Player)event.getEntity();
//
//		Raider r=Raider.getRaider(p);
//		if(p.getLocation().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))&&!r.isInDungeon()&&!r.getInvading()&&!r.getArbiter()){
//			event.setCancelled(true);
//		}
//		if(((p.getHealth() - event.getDamage() < 1)&&p.getWorld().equals(Bukkit.getWorld(Constants.dunegonW)))||(p.getFireTicks()>0&&p.getHealth()-event.getDamage()<=1)){
//			event.setCancelled(true);
//			if(r.isInDungeon()){
//				p.teleport(Constants.res);
//			}else{
//				p.teleport(Constants.exit);
//			}
//			p.setHealth(p.getMaxHealth());
//			extinguish(p);
//			for(PotionEffect pe:r.getPlayer().getActivePotionEffects()){
//				Dungeons.getPlugin(Dungeons.class).log(r.getName());
//				r.getPlayer().removePotionEffect(pe.getType());
//			}
//			p.setHealth(p.getMaxHealth());
//			p.setFoodLevel(20);
//
//
//			if(!r.getDead()){
//				r.setDead(true);
//				if(r.hasGroup()){
//					r.getGroup().notifyDeath((Player)event.getEntity());
//				}
//			}
//
//		}else if((p.getHealth() - event.getDamage() < 1)&&r.getInvading()){
//			event.setCancelled(true);
//			//p.playEffect(EntityEffect.DEATH);
//			p.teleport(Constants.exit);
//			p.setHealth(p.getMaxHealth());
//			extinguish(p);
//			for(PotionEffect pe:r.getPlayer().getActivePotionEffects()){
//				Dungeons.getPlugin(Dungeons.class).log(r.getName());
//				r.getPlayer().removePotionEffect(pe.getType());
//			}
//			p.setHealth(p.getMaxHealth());
//			p.setFoodLevel(20);
//			p.sendMessage(ChatColor.RED+"YOU DIED!!, Invasion Failed");
//			if(!r.getDead()){
//				r.setDead(true);
//				if(r.hasGroup()){
//					r.getGroup().notifyDeath((Player)event.getEntity());
//				}
//
//			}
//		}
//	}

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

