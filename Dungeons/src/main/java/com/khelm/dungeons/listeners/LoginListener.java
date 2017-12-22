package com.khelm.dungeons.listeners;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.DatabaseWorker;
import com.khelm.dungeons.Raider;
import com.khelm.dungeons.cmdexecutors.DMCommandExecutor;
 
/**
 * Handles event listeners for join,login and quit events
 * 
 * @author IraqiMilitant
 *
 */
public final class LoginListener implements Listener {
	

	/**
	 * on login get the player info from database
	 * 
	 * @param event
	 */
    @EventHandler
    public void normalLogin(PlayerLoginEvent event) {
    	Player p=event.getPlayer();
        DatabaseWorker.readPlayerInfo(p);
        DatabaseWorker.login(Raider.getRaider(p));
    }
    
    /**
     * on player join ensure that if they logged off in the dungeon world they are moved to the hub
     * 
     * @param event
     */
    @EventHandler
    public void normalJoin(PlayerJoinEvent event) {
    	Player p=event.getPlayer();
        if(p.getLocation().getWorld()==Bukkit.getWorld(Constants.dunegonW)){
        	p.teleport(new Location(Bukkit.getWorld(Constants.dunegonW),Constants.exitX,Constants.exitY,Constants.exitZ));
        }
    }
    
    /**
     * on quit make the player leave their group and write their data to database
     * @param event
     */
    @EventHandler
    public void normalQuit(PlayerQuitEvent event) {
    	Raider r=Raider.getRaider(event.getPlayer());
    	if(r.hasGroup()){
    		r.leaveGroup();
    	}
    	for(PotionEffect pet:event.getPlayer().getActivePotionEffects()){
    		event.getPlayer().removePotionEffect(pet.getType());
    	}
    	event.getPlayer().setAllowFlight(false);
    	r.logOff();
    	
    	DatabaseWorker.writePlayerInfo(r);
    	DMCommandExecutor.getExecutor().remove(event.getPlayer());
    	
    }
}