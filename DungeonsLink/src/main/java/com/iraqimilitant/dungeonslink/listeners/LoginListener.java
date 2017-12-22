package com.iraqimilitant.dungeonslink.listeners;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;

import com.iraqimilitant.dungeonslink.Constants;
import com.iraqimilitant.dungeonslink.DatabaseWorker;
import com.iraqimilitant.dungeonslink.Raider;
 
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
    }
   
    
    /**
     * on quit make the player leave their group and write their data to database
     * @param event
     */
    @EventHandler
    public void normalQuit(PlayerQuitEvent event) {
    	Raider r=Raider.getRaider(event.getPlayer());
    	event.getPlayer().setAllowFlight(false);
    	for(PotionEffect pet:event.getPlayer().getActivePotionEffects()){
    		event.getPlayer().removePotionEffect(pet.getType());
    	}
    	r.logOff();
    	
    	//DatabaseWorker.writePlayerInfo(r);
    	
    }
}