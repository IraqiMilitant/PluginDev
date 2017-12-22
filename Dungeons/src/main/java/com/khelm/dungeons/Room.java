package com.khelm.dungeons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.khelm.dungeons.generation.GenerationAlgorithm;
import com.khelm.dungeons.mobcontrol.MobGroup;

/**
 * Holds all data and methods for handling rooms, is serializable as it saves to file as part of the dungeon file
 * 
 * @author IraqiMilitant
 *
 */
public class Room implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6882327986690786159L;

	private SerializableLocation corner1;
	private SerializableLocation corner2;


	private int roomNum;
	//base difficulty of the room
	private int baseDiff;
	//current group in the room (if any)
	private transient RaidGroup currentGroup;
	//exit, entrance, and center-points of the dungeon
	private SerializableLocation entry,exit,center;
	//is the room occupied
	private boolean occupied;
	//is it the last room in its' dungeon
	private boolean last;
	//radius of the dungeon (from center to farthest edge)
	double size;
	//room that comes after this
	private Room nextRoom;
	//dungeon that this room resides in
	private Dungeon dungeon;
	//axis bounds of the room
	public int lowX,lowY,lowZ,highX,highY,highZ;
	//list of locations that are kept as Air
	private ArrayList<SerializableLocation>keepAir;
	//the assigned theme of the room
	private transient Theme theme;
	//for saving theme to file
	private String themeSave;
	//list of potion effects assigned to the room
	private transient ArrayList<PotionEffectType>effects;
	//string list used to save potioneffects when the room is saved to file
	private ArrayList<String>effectsSave;
	//identifies if effects have been saved
	private boolean effectsSaved;
	//the assigned hazard for the room
	private Hazard hazard;
	//how the room generates (based on theme data)
	private GenerationAlgorithm gen;
	//if a hazard is set
	private boolean hazardSet;
	//if hazard was geenrated
	private boolean hazardGen;
	//MobGroups assigned to the room
	private ArrayList<MobGroup> groups;
	private double diffMod=0;

	private transient boolean invadeMe;
	private transient Raider invader;

	/**
	 * Constructor builds a room given 2 opposing corners, an exit point, an entrance point
	 * and the dungeon that will hold it
	 * 
	 * @param corner1
	 * @param corner2
	 * @param entry
	 * @param exit
	 * @param dungeon
	 */
	public Room(Location corner1,Location corner2,Location entry,Location exit,Dungeon dungeon){
		this.corner1=new SerializableLocation(corner1);
		this.corner2=new SerializableLocation(corner2);
		this.entry=new SerializableLocation(entry);
		this.exit=new SerializableLocation(exit);
		groups=new ArrayList<MobGroup>();
		hazardSet=false;
		hazardGen=false;
		this.hazard=Hazard.NONE;
		invadeMe=false;
		last=false;
		baseDiff=0;
		this.dungeon=dungeon;
		keepAir=new ArrayList<SerializableLocation>();
		effects=new ArrayList<PotionEffectType>();
		setCenter();
		setAirBlocks();
		clearRoom(true);
		themeSave=null;
	}

	/**
	 * returns true if the room has minimum config needed to be used in an open dungeon
	 * 
	 * @param p
	 * @return
	 */
	public boolean validate(Player p){
		if(!(theme instanceof Theme)){
			p.sendMessage(ChatColor.RED+"Room "+this.roomNum+" is not assigned a theme");
			return false;
		}
		if(!(groups instanceof ArrayList<?>)){
			p.sendMessage(ChatColor.RED+"Room "+this.roomNum+" has no assigned mobs");
			return false;
		}else{
			if(groups.isEmpty()){
				p.sendMessage(ChatColor.RED+"Room "+this.roomNum+" has no assigned mobs");
				return false;
			}
		}
		if(this.evaluateDiff()<=0){
			return false;
		}



		return true;
	}

	/**
	 * add a group of mobs to the room
	 * 
	 * @param m
	 */
	public void addMobGroup(MobGroup m){
		groups.add(m);
	}

	/**
	 * remove a group of mobs
	 * 
	 * @param name
	 */
	public void removeMobGroup(String name){
		MobGroup target=null;
		boolean flag=false;
		for(MobGroup m:groups){
			if(m.getName().toUpperCase().equals(name.toUpperCase())){
				target=m;
				flag=true;
				break;
			}
		}
		if(flag){
			groups.remove(target);
		}
	}

	public ArrayList<MobGroup> getMobGroups(){
		return groups;
	}

	public void clearMobGroups(){
		groups.clear();
	}

	public Dungeon getDungeon(){
		return this.dungeon;
	}

	/**
	 *builds the list of locations to keep as air
	 */
	private void setAirBlocks(){
		Location l;
		for(int x=lowX;x<highX+1;x++){
			for(int y=lowY;y<highY+1;y++){
				for(int z=lowZ;z<highZ+1;z++){
					l=new Location(center.getLocation().getWorld(),x,y,z);
					if(l.getBlock().getType()==Material.BEDROCK){
						keepAir.add(new SerializableLocation(l));
					}
				}
			}
		}
	}

	/**
	 *returns the center point of the room 
	 * 
	 * @return
	 */
	public Location getCenter(){
		return center.getLocation();
	}

	public int evaluateDiff(){
		baseDiff=0;
		for(PotionEffectType p:effects){
			baseDiff+=Constants.effects.get(p.getName());
		}
		for(MobGroup mg:groups){
			baseDiff+=mg.getDiff();
		}
		if(hazardGen){
			baseDiff+=Constants.Hazards.get(hazard.toString());
		}
		return baseDiff;
	}

	/**
	 * returns location of the dungeon exit
	 * 
	 * @return
	 */
	public Location getExit(){
		return exit.getLocation();
	}

	/**
	 * returns location of dungeon entrance
	 * 
	 * @return
	 */
	public Location getEntry(){
		return entry.getLocation();
	}

	/**
	 * adds a potion efefct to the dungeon
	 * 
	 * @param e
	 */
	public void addEffect(PotionEffectType e){
		if(!effects.contains(e)){
			effects.add(e);
		}
	}

	/**
	 * removes a potion effect from the dungeon (if exists)
	 * 
	 * @param pe
	 * @return
	 */
	public boolean removeEffect(PotionEffectType pe){
		if(effects.contains(pe)){
			effects.remove(pe);
			return true;
		}

		return false;
	}

	public void clearRoomEffects(){
		if(effects instanceof ArrayList<?>){
			effects.clear();
		}
	}

	/**
	 * applies all set potion effects to the players entering the room
	 * 
	 */
	private void applyEffects(){
		Player pl;
		for(Raider r:currentGroup.getGroup()){//loop players
			pl=r.getPlayer();
			for(PotionEffectType p:effects){//loop effects
				pl.addPotionEffect(new PotionEffect(p,10000,1));
			}
		}
	}

	/**
	 * set the room's hazard
	 * 
	 * @param hazard
	 */
	public void setHazard(Hazard hazard){
		this.hazard=hazard;
		if(this.hazard==Hazard.NONE){
			hazardSet=false;
		}else{
			hazardSet=true;
			if(!(theme instanceof Theme)){
				hazardGen=gen.Hazard(theme, this);
			}
		}
		buildRoom();
	}

	/**
	 * get the room's hazard
	 * 
	 * @return
	 */
	public Hazard getHazard(){
		return hazard;
	}

	/**
	 * sets the locations to air which are found in the keepAir list
	 */
	private void setAir(){
		if(keepAir.size()>0){
			for(SerializableLocation l:keepAir){
				l.getLocation().getBlock().setType(Material.AIR);
			}
		}
	}

	private void safeEntrance(){
		entry.getLocation().getBlock().setType(Material.AIR);
		for(BlockFace bf:BlockFace.values()){
			if(entry.getLocation().getBlock().getRelative(bf).getType().equals(Material.FIRE)){
				entry.getLocation().getBlock().getRelative(bf).setType(Material.AIR);
			}

			if(entry.getLocation().getBlock().getRelative(bf).getType().equals(Material.LAVA)){
				entry.getLocation().getBlock().getRelative(bf).setType(Material.AIR);
			}
		}
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH_EAST).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH_WEST).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH_EAST).getLocation(), false, false, this);
		theme.generateBlock(entry.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH_WEST).getLocation(), false, false, this);
	}

	/**
	 * define the room center point
	 */
	private void setCenter(){
		double x,y,z;

		Location c1=corner1.getLocation();
		Location c2=corner2.getLocation();

		lowX=0;
		highX=0;

		lowY=0;
		highY=0;

		lowZ=0;
		highZ=0;

		if (c1.getX()<c2.getX()){
			x=c1.getX()+(c2.getX()-c1.getX())/2;
			lowX=c1.getBlockX();
			highX=c2.getBlockX();

		}else{
			x=c2.getX()+(c1.getX()-c2.getX())/2;
			lowX=c2.getBlockX();
			highX=c1.getBlockX();

		}

		if (c1.getY()<c2.getY()){
			y=c1.getY()+(c2.getY()-c1.getY())/2;
			lowY=c1.getBlockY();
			highY=c2.getBlockY();

		}else{
			y=c2.getY()+(c1.getY()-c2.getY())/2;
			lowY=c2.getBlockY();
			highY=c1.getBlockY();

		}

		if (c1.getZ()<c2.getZ()){
			z=c1.getZ()+(c2.getZ()-c1.getZ())/2;
			lowZ=c1.getBlockZ();
			highZ=c2.getBlockZ();

		}else{
			z=c2.getZ()+(c1.getZ()-c2.getZ())/2;
			lowZ=c2.getBlockZ();
			highZ=c1.getBlockZ();

		}

		center=new SerializableLocation(new Location(Bukkit.getWorld(Constants.dunegonW),x,y,z));
		size=c1.distance(c2)/2;



	}

	/**
	 * checks if the room is clear (all enemies dead)
	 * 
	 * @return
	 */
	public boolean roomClear(){
		World w=center.getLocation().getWorld();
		for(Entity e:w.getEntities()){
			if((e instanceof LivingEntity) && !(e instanceof Player)){

				if(isInside(e.getLocation())){
					return false;
				}
			}
		}



		return true;
	}

	/**
	 * clears the room of entities and blocks (if build true)
	 * 
	 * @param build
	 */
	public void clearRoom(boolean build){
		Location l;
		//clear mobs
		
		for(Entity e:Bukkit.getWorld(Constants.dunegonW).getEntities()){
			if(!(e instanceof Player)){
				if(isInside(e.getLocation())){
					e.remove();
				}
			}
		}
		if(build){
			for(int x=lowX;x<highX+1;x++){
				for(int y=lowY;y<highY+1;y++){
					for(int z=lowZ;z<highZ+1;z++){
						l=new Location(center.getLocation().getWorld(),x,y,z);
						l.getBlock().setType(Material.AIR);

					}
				}

			}

		}
	}


	/**
	 * sets the room's theme and triggers a regeneration of the room
	 * 
	 * @param theme
	 */
	public void setTheme(Theme theme){
		this.theme=theme;
		gen=theme.getGen();
		buildRoom();
	}

	/**
	 * builds the room
	 */
	@SuppressWarnings("deprecation")
	public void buildRoom(){
		if(!(theme instanceof Theme)){//if the theme is not set then return as can't build without a theme
			return;
		}
		gen.Generate(theme, this);//use the rooms generation Algorithm to generate the room
		setAir();//ensure defined airblocks are air
		if(hazardSet){//if there is a room hazard
			hazardGen=gen.Hazard(theme, this);//tell generation algorithm to add the hazard
		}

		gen.genTorches(theme, this);

		//mark the exit
		Location l=exit.getLocation();
		l.getBlock().setType(Material.getMaterial(Constants.marker));
		l.getBlock().getRelative(BlockFace.UP).setType(Material.getMaterial(Constants.marker));
		safeEntrance();
	}

	/**
	 * determine if there will be an invasion
	 * 
	 * @param rating
	 * @return
	 */
	private boolean invadeChance(int rating){
		Random rand=new Random();
		ArrayList<Raider>queue=Raider.getInvaderQueue();
		if (!queue.isEmpty()){
			if(rand.nextBoolean()){
				for(Raider r:queue){
					if(Math.abs(r.getLocalRating()-rating)<(20+(0.25*rating))){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * find an arbiter
	 * 
	 * @return
	 */
	private Raider arbiter(){
		int rating;
		ArrayList<Raider>queue=Raider.getArbiterQueue();
		ArrayList<Raider>options=new ArrayList<Raider>();
		Raider arb=null;
		if (!queue.isEmpty()){
			for(Raider ra:currentGroup.getGroup()){
				rating=ra.getLocalRating();
				if(ra.getSin()>10){
					for(Raider r:queue){
						if(Math.abs(r.getLocalRating()-rating)<(20+(ra.getSin()>20?ra.getSin()-20:5)+(0.30*rating))){
							options.add(r);
						}
					}
				}
			}

			if(!options.isEmpty()){
				for(Raider r:options){
					if(arb instanceof Raider){
						if(r.getLocalRating()>arb.getLocalRating()){
							arb=r;
						}
					}else{
						arb=r;
					}
				}
				return arb;
			}

		}
		return null;
	}

	/**
	 * react to the group in the room completely wiping
	 */
	public void groupDeath(){
		if(invadeMe && invader instanceof Raider){
			invader.notifyInvasionWin();
		}
		this.clearRoom(false);
	}

	/**
	 * spawn all defined mobs
	 */
	public void spawnMobs(int rating){
		ArrayList<SerializableLocation>locs=new ArrayList<SerializableLocation>();
		ArrayList<Raider>queue=Raider.getInvaderQueue();
		ArrayList<Raider>options=new ArrayList<Raider>();
		int x=0;
		int y=0;
		int z=0;
		Location l;
		Random r=new Random();

		for(y=lowY;y<(highY-((highY-lowY)/2));y++){
			for(x=lowX+2;x<highX-2;x++){
				for(z=lowZ+2;z<highZ-2;z++){
					l=new Location(Bukkit.getWorld(Constants.dunegonW),x,y,z);
					if(l.distance(entry.getLocation())>10){
						if (l.getBlock().getType().equals(Material.AIR)){
							if (!(l.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR))){
								if((l.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.AIR))&&(l.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.AIR))&&(l.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.AIR))&&(l.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.AIR))){
									if((l.getBlock().getRelative(BlockFace.NORTH_EAST).getType().equals(Material.AIR))&&(l.getBlock().getRelative(BlockFace.NORTH_WEST).getType().equals(Material.AIR))&&(l.getBlock().getRelative(BlockFace.SOUTH_EAST).getType().equals(Material.AIR))&&(l.getBlock().getRelative(BlockFace.SOUTH_WEST).getType().equals(Material.AIR))){
										locs.add(new SerializableLocation(l));
									}
								}
							}
						}
					}
				}
			}

		}
		SerializableLocation ls;
		for(MobGroup m:groups){
			if(locs.size()>0){
				ls=locs.get(Math.abs(r.nextInt(locs.size())));
				m.spawnGroup(ls.getX(),ls.getY(),ls.getZ(), diffMod>1?diffMod+(diffMod/4):1, diffMod>=2?diffMod:diffMod*0.75,(hazard instanceof Hazard)?hazard.fireDamage():false);
			}else{
				Dungeons.getPlugin(Dungeons.class).log("no spawn loc");
			}
		}
		if(invadeMe){
			for(Raider raid:queue){
				if(!raid.isInDungeon()){
					if(Math.abs(raid.getLocalRating()-rating)<(20+(0.25*rating))){
						options.add(raid);
					}
				}
			}
			if(!options.isEmpty()){
				ls=locs.get(Math.abs(r.nextInt(locs.size()-1)));
				invader=options.get(Math.abs(r.nextInt(options.size())));
				invader.getPlayer().teleport(ls.getLocation());
				invader.setInvading(true);
				this.currentGroup.setInvader(true,false);
				this.currentGroup.tell(ChatColor.RED+"YOU HAVE BEEN INVADED BY "+invader.getPlayer().getDisplayName()+"!!");
			}
		}else{
			Raider arbiter=arbiter();
			if(arbiter instanceof Raider){
				ls=locs.get(Math.abs(r.nextInt(locs.size()-1)));
				arbiter.getPlayer().teleport(ls.getLocation());
				arbiter.setArbiter(true);
				invadeMe=true;
				this.currentGroup.setInvader(true,true);
				this.currentGroup.tell(ChatColor.BLUE+"An Arbiter Approaches");

			}
		}



	}

	/**
	 * sets the room to last room
	 */
	public void setLast(){
		last=true;
	}

	/**
	 * gets whether or not this room is the last in the dungeon
	 * 
	 * @return
	 */
	public boolean getLast(){
		return last;
	}

	/**
	 * gets the room following this one
	 * 
	 * @return
	 */
	public Room getNextRoom(){
		return nextRoom;
	}

	/**
	 * sets the rooms number
	 * 
	 * @param roomNum
	 */
	public void setRoomNum(int roomNum){
		this.roomNum=roomNum;
	}

	/**
	 * clear potion effects of the current group
	 * 
	 */
	private void clearEffects(){
		for(Raider r:currentGroup.getGroup()){
			for(PotionEffect p:r.getPlayer().getActivePotionEffects()){
				r.getPlayer().removePotionEffect(p.getType());
				r.getPerk().applyEffect(r.getPlayer());
			}
		}
	}

	/**
	 * sends the room info to a player
	 * 
	 * @param p
	 */
	public void printRoomInfo(Player p){
		this.evaluateDiff();
		p.sendMessage(ChatColor.GREEN+"Room: "+ChatColor.YELLOW+this.roomNum);
		p.sendMessage(ChatColor.GREEN+"Base Difficulty: "+ChatColor.YELLOW+this.baseDiff);

		p.sendMessage(ChatColor.GREEN+"Room Effects: ");
		for(PotionEffectType pe:this.effects){
			p.sendMessage(ChatColor.YELLOW+"    "+pe.getName());
		}		

		p.sendMessage(ChatColor.GREEN+"Mob Groups: ");
		for(MobGroup mg:this.groups){
			p.sendMessage(ChatColor.YELLOW+"    "+mg.getName());
		}
		p.sendMessage(ChatColor.GREEN+"Hazard: "+ChatColor.YELLOW+this.hazard);
		if(theme instanceof Theme){
			p.sendMessage(ChatColor.GREEN+"Theme: "+ChatColor.YELLOW+this.theme.getName());
		}
	}

	/**
	 * add a group to the room if possible
	 * 
	 * @param rg
	 * @return
	 */
	public boolean setGroup(RaidGroup rg){
		if(!occupied){//is the room already occupied?
			occupied=true;//set occupied
			invader=null;
			currentGroup=rg;//assign current group
			
			rg.setRoom(this);//set that group's room
			moveGroup(rg);//move thr group into the room
			clearRoom(false);//clear all mobs in the room
			double adjust;

			if(rg.getGroupRating()<baseDiff*rg.getGroup().size()){
				if((baseDiff*rg.getGroup().size()-rg.getGroupRating())>=5){
					adjust=baseDiff*rg.getGroup().size()-rg.getGroupRating();
					adjust=((int)adjust)/(int)5;
					diffMod=1;
					for(int i=1;i<=adjust;i++){
						diffMod=diffMod*0.85;
					}
				}else{
					diffMod=1;
				}
			}else if(rg.getGroupRating()>baseDiff*rg.getGroup().size()){
				if((rg.getGroupRating()-baseDiff*rg.getGroup().size())>=10){
					adjust=rg.getGroupRating()-baseDiff*rg.getGroup().size();
					adjust=((int)adjust)/(int)10;
					diffMod=1;
					for(int i=1;i<=adjust;i++){
						diffMod=diffMod*1.10;
					}
				}else{
					diffMod=1;
				}
			}else{
				diffMod=1;
			}
			if (diffMod<0.3){
				diffMod=0.3;
			}
			invadeMe=invadeChance(rg.getGroupRating()/rg.getGroup().size());
			final RaidGroup rgrp=rg;
			Dungeons.getPlugin(Dungeons.class).getServer().getScheduler().scheduleSyncDelayedTask(Dungeons.getPlugin(Dungeons.class),new Runnable(){

				public void run() {
					spawnMobs(rgrp.getGroupRating()/rgrp.getGroup().size());

				}


			}, 40L);

			//spawn mobs defined for the room
			clearEffects();//clear all effects from the group
			applyEffects();//set the effects for this room on the players
			return true;
		}else{
			rg.tell("That dungeon room is currently occupied");
		}
		return false;
	}

	/**
	 * set the next room
	 * 
	 * @param r
	 */
	public void setNext(Room r){
		this.nextRoom=r;
	}

	/**
	 * get the current group in the room
	 * 
	 * @return
	 */
	public RaidGroup getGroup(){
		return currentGroup;
	}

	/**
	 * move the group into the room
	 * 
	 * @param rg
	 * @return
	 */
	public boolean moveGroup(RaidGroup rg){
		if(roomNum==1){
			rg.moveGroup(entry.getLocation());
		}else{
			rg.moveGroup(entry.getLocation());
		}

		return true;
	}

	/**
	 * called when the group attempts to leave
	 */
	public void progress(){

		if(roomClear()){//if there are no enemies left
			if(last){//if the room is the last one
				clearEffects();//clear effects on the group
				dungeon.finish(currentGroup);//trigger dungeon finish
				this.removeGroup();//remove the group
			}else{
				if(nextRoom.setGroup(this.currentGroup)){//if move into next room was successful
					this.removeGroup();//remove the group
				}

			}
		}

	}

	/**
	 * remove group from the room
	 */
	public void removeGroup(){

		currentGroup=null;
		occupied=false;
		//Will need to reset the room as well (kill mobs etc)
	}

	/**
	 * get the room number
	 * 
	 * @return
	 */
	public int getRoomNum(){
		return roomNum;
	}

	/**
	 * check if a location is near the exit point
	 * 
	 * @param l
	 * @return
	 */
	public boolean nearExit(Location l){
		if(l.getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
			if (l.distance(this.exit.getLocation())<=2){
				return true;
			}
		}
		return false;
	}

	/**
	 * check if a point is inside the room
	 * 
	 * @param l
	 * @return
	 */
	public boolean isInside(Location l){
		boolean x=false;
		boolean y=false;
		boolean z=false;
		Location c1=corner1.getLocation();
		Location c2=corner2.getLocation();
		if (c1.getBlockX()<c2.getBlockX()){
			if(l.getBlockX()>=c1.getBlockX()&&l.getBlockX()<=c2.getBlockX()){
				x=true;
			}
		}else{
			if(l.getBlockX()>=c2.getBlockX()&&l.getBlockX()<=c1.getBlockX()){
				x=true;
			}
		}

		if (c1.getBlockY()<c2.getBlockY()){
			if(l.getBlockY()>=c1.getBlockY()&&l.getBlockY()<=c2.getBlockY()){
				y=true;
			}
		}else{
			if(l.getBlockY()>=c2.getBlockY()&&l.getBlockY()<=c1.getBlockY()){
				y=true;
			}
		}

		if (c1.getBlockZ()<c2.getBlockZ()){
			if(l.getBlockZ()>=c1.getBlockZ()&&l.getBlockZ()<=c2.getBlockZ()){
				z=true;
			}
		}else{
			if(l.getBlockZ()>=c2.getBlockZ()&&l.getBlockZ()<=c1.getBlockZ()){
				z=true;
			}
		}


		if(x&&y&&z){
			return true;
		}

		return false;
	}

	/**
	 * prep the room to be saved to file
	 */
	public void prepForSave(){
		this.occupied=false;
		this.currentGroup=null;
		if(this.theme instanceof Theme){
			themeSave=theme.getName();
		}
		effectsSave=new ArrayList<String>();
		for(MobGroup m:groups){
			m.prepForSave();
		}
		if(effects.size()>0){//if there are effects save them to string
			for(PotionEffectType p:effects){
				effectsSave.add(p.getName());
				effectsSaved=true;
			}
		}
	}


	/**
	 * reload the items stored as string and tell mobgroups to do the same
	 * 
	 */
	public void load(){
		effects=new ArrayList<PotionEffectType>();
		if(effectsSaved){//if effects were saved
			for(String s:effectsSave){
				if(!effects.contains(PotionEffectType.getByName(s))){
					effects.add(PotionEffectType.getByName(s));
				}
			}
		}

		if(themeSave!=null){
			theme=Theme.getTheme(themeSave);
		}

		for(MobGroup m:groups){
			if(!m.load()){
				groups.remove(m);
			}
		}
		effectsSaved=false;
	}

}
