package com.khelm.dungeons.listeners;

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

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;
import com.khelm.dungeons.cmdexecutors.DMCommandExecutor;
import com.khelm.dungeons.cmdexecutors.DungeonCommandExecutor;
import com.khelm.dungeons.cmdexecutors.GearCommandExecutor;
import com.khelm.dungeons.uniqueitems.armour.ArmourSet;

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
	 * on player interact check with the DungeonCommandExecutor if it should be cancelled
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event) {
		if(event.getItem() instanceof ItemStack){
			if(event.getItem().hasItemMeta()){
				DMCommandExecutor.getExecutor().itemInit(event.getPlayer(), event.getItem());
			}

		}

		if(event.getClickedBlock()instanceof Block){
			if(DungeonCommandExecutor.getCommandExecutor().blockSelect(event.getClickedBlock().getLocation(),event.getPlayer())){
				event.setCancelled(true);
			}
		}
	}

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
	 * pass player move events to relevant instances
	 * @param event
	 */
	@EventHandler
	public void PlayerMoveEvent(PlayerMoveEvent event) {
		GearCommandExecutor.playerMove(event.getPlayer());
		Raider r=Raider.getRaider(event.getPlayer());

		if(event.getPlayer().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			if(event.getPlayer().isFlying()&&!event.getPlayer().isOp()&&!(Raider.getRaider(event.getPlayer()).getEdits())){
				event.getPlayer().setFlying(false);
				event.getPlayer().setVelocity(new Vector(0,0,0));
			}
		}
		if(r.isInDungeon()){
			if(r.getGroup().getLeader()==r){
				if(r.getGroup().getRoom().nearExit(event.getPlayer().getLocation())){
					r.getGroup().getRoom().progress();
				}
			}
		}

		if(event.getPlayer().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			for(ArmourSet ae:r.getArmourSets().keySet()){
				ae.ApplyEffects(event,r.getArmourSets().get(ae));
			}
		}
	}

	/**
	 * deals with death events for players (need to know when a player dies in a dungeon)
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent event){
		Raider r=Raider.getRaider(event.getEntity());
		if(r.hasGroup()){
			if(!(r.getDead())){
				r.getGroup().notifyDeath(event.getEntity());
				r.setDead(true);
			}
		}
	}

	/**
	 * handles respawns
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		Raider r=Raider.getRaider(event.getPlayer());
		if(r.hasGroup()&&!r.isInDungeon()){
			r.getGroup().notifyRes();
			r.setDead(false);
		}else if(r.isInDungeon()){
			event.setRespawnLocation(Constants.res);
		}

		if(event.getPlayer().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			Raider.getRaider(event.getPlayer()).updateArmour();
		}

	}

	/**
	 * Used to block chat from Invaders and Arbiters
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerChatEvent(AsyncPlayerChatEvent event){
		if(Raider.getRaider(event.getPlayer()).getInvading()||Raider.getRaider(event.getPlayer()).getArbiter()){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED+"Invaders and Arbiters are not permitted to chat");
		}
	}

	/**
	 * used to block item drops to prevent players from trading items
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerDropItemEvent(PlayerDropItemEvent event){
		if(event.getPlayer().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			if(!event.getPlayer().isOp()){
				event.setCancelled(true);
			}
		}

	}

	/**
	 * triggers a armour set update on item break (incase a set piece breaks)
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerItemBreakEvent(PlayerItemBreakEvent event){
		if(event.getPlayer().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			Raider.getRaider(event.getPlayer()).updateArmour();
		}
	}

	/**
	 * Blocks all commands but what is on the whitelist for any without bypass perms
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event){
		if(!event.getPlayer().hasPermission("dungeons.admin")&&event.getPlayer().getLocation().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){//add perm node here instead
			Raider r=Raider.getRaider(event.getPlayer());
			if(r.isInDungeon()||r.getInvading()||r.getArbiter()){
				event.setCancelled(true);
				for(String s:Constants.commands){
					if(event.getMessage().contains(s)&&event.getPlayer().getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
						r=Raider.getRaider(event.getPlayer());
						if(r.isInDungeon()||r.getInvading()||r.getArbiter()||r.getEdits()){
							event.setCancelled(false);
						}
					}

				}
			}
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

