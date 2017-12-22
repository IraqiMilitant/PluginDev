package com.iraqimilitant.dungeonslink;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;


/**
 * Class which sits ontop of the Player class (note does not extend). 
 * 
 * @author IraqiMilitant
 *
 */
public class Raider{

	private static ArrayList<Raider>raiders=new ArrayList<Raider>();

	//the corresponding player object
	private Player player;
	//local and global rating of the player
	private int rating=0;

	private int lastHit=0;

	private DungeonPerks activePerk;


	/**
	 * Raider constructor, built from the Player object
	 * 
	 * @param p
	 */
	public Raider(Player p){
		player=p;
		this.activePerk=DungeonPerks.NONE;
		raiders.add(this);

	}

	public void setLastHit(int id){
		this.lastHit=id;
	}

	public int getLastHit(){
		return lastHit;
	}

	public void togglePerk(String name){
		try{
			DungeonPerks perk=DungeonPerks.valueOf(name.toUpperCase());
			if(this.rating>=perk.rating){
				if(perk.equals(activePerk)){
					activePerk.clearEffects(this.player);
					activePerk=DungeonPerks.NONE;
					this.player.sendMessage(ChatColor.YELLOW+perk.getName()+"Perk turned off");
					return;
				}

				perk.applyEffect(this.player);
				activePerk=perk;
				this.player.sendMessage(ChatColor.GREEN+perk.getName()+"Perk turned on");

			}else{
				this.player.sendMessage(ChatColor.RED+"You need "+ChatColor.BLUE+perk.rating+ChatColor.RED+" rating to use that perk, you have "+ChatColor.BLUE+this.rating);
			}
		}catch(Exception e){
			this.player.sendMessage(ChatColor.RED+"No such perk");
		}
	}

	public void notifyDamaged(EntityDamageEvent event){

		activePerk.applyEffect(player, event);

	}

	public void notifyTargetted(EntityTargetEvent event){

		activePerk.applyEffect(player, event);


	}

	public DungeonPerks getPerk(){
		return activePerk;
	}

	/**
	 * gets the player's Local rating
	 * 
	 * @return
	 */
	public int getRating(){
		return rating;
	}

	/**
	 * get the corresponding player object
	 * 
	 * @return
	 */
	public Player getPlayer(){
		return player;
	}

	/**
	 * Get the player's name
	 * 
	 * @return
	 */
	public String getName(){
		return player.getName();
	}

	/**
	 * sets the local rating of the player, and saves the change to the database
	 * 
	 * @param lr
	 * @param fromDB
	 */
	public void setRating(int lr){
		DungeonsLink.logToFile(this.player.getDisplayName()+"'s rating set to "+lr+", previously "+this.rating);
		rating=lr;
	}

	/**
	 * when the player logs off ref to them is removed from the Raider list
	 */
	public void logOff(){
		raiders.remove(this);

	}

	/**************************************
	 * 
	 * STATIC METHODS
	 * 
	 **************************************/

	/**
	 * get a raider based on a player object
	 * 
	 * @param p
	 * @return
	 */
	public static Raider getRaider(Player p){
		for(Raider r: raiders){
			if(r.getPlayer()==p){
				return r;
			}
		}

		return null;
	}

	/**
	 * Get the raider list
	 * 
	 * @return
	 */
	public static ArrayList<Raider> getRaiders(){
		return raiders;
	}


}
