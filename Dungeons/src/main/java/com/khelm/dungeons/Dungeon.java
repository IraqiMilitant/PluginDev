package com.khelm.dungeons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Serializable class which holds all data and methods relevant to control of an individual dungeon. 
 * This class is serialized and saved to file as a method of saving defined dungeons between server restarts
 * 
 * note transient means that data is not saved on serialization
 * 
 * @author IraqiMilitant
 *
 */
public class Dungeon implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 904313849661548211L;
	
	//static arraylist which holds the list of all defined dungeons,
	private transient static ArrayList<Dungeon>dungeons=new ArrayList<Dungeon>();
	//static array list of strings to hold dungeon names
	private transient static ArrayList<String>dungeonNames;

	//list of rooms in the defined dungeon
	private ArrayList<Room> rooms;
	//current groups that are attempting the dungeon
	private transient ArrayList<RaidGroup> currentGroups;
	//the base difficulty value of the dungeon
	private int baseDiff;
	
	private int topDiff;
	//how many rooms in the dungeon
	private int roomCount;
	//name of the dungeon
	private String name;
	//teh final room in the dungeon
	private Room last;
	//ref of current room being edited, used for when a dungeonmaster is editing
	private transient Room editing;
	//boolean on if a room is selected for edit
	private transient boolean roomSelected=false;

	//is the dungeon ready to be attempted
	private boolean ready;
	//is the dungeon owned
	private boolean owned;
	//who owns the dungeon
	private String owner;
	private String ownerName;
	//when did the owner take possession
	private Date possessionDate;


	/**
	 * dungeon constructor, defines a new dungeon
	 *  
	 * @param name
	 */
	public Dungeon(String name){
		rooms=new ArrayList<Room>();
		currentGroups=new ArrayList<RaidGroup>();
		baseDiff=0;
		roomCount=0;
		this.name=name;
		ready=false;
		owned=false;
	}

	/**
	 * assigns a player to a dungeon for editing (DM stuff)
	 * 
	 * @param p Player
	 */
	public void assign(Player p){
		owned=true;
		this.ready=false;
//		if(owner==null){
		owner=p.getUniqueId().toString();
		ownerName=p.getDisplayName();
		possessionDate=new Date();
//		}
//		if(!owner.equals(p.getUniqueId().toString())){
//			owner=p.getUniqueId().toString();
//			possessionDate=new Date();
//		}
		
	}
	
	public String getOwner(){
		return ownerName;
	}

	/**
	 * returns the date and time that the current owner took possession
	 * @return
	 */
	public Date getDate(){
		return possessionDate;
	}

	/**
	 * adds a given room to the dungeon
	 * 
	 * @param room
	 */
	public void newRoom(Room room){
		roomCount++;
		room.setRoomNum(roomCount);
		rooms.add(room);
		last=room;

	}
	
	public boolean validate(Player p){
		boolean result=true;
		for(Room r: rooms){
			if(!r.validate(p)){
				result= false;
			}
		}
		
		return result;
	}

	/**
	 * replaces a target room with a given room
	 * 
	 * @param r
	 * @param roomNum
	 */
	public void replaceRoom(Room r,int roomNum){
		r.setRoomNum(roomNum);
		for(Room cr:rooms){
			if(roomNum>1){
				if(cr.getRoomNum()==(roomNum-1)){
					cr.setNext(r);
				}else if(cr.getRoomNum()==(roomNum)){
					rooms.remove(cr);
					rooms.add(r);
					if(cr.getLast()){
						r.setLast();
					}else{
						r.setNext(cr.getNextRoom());
						cr.setNext(null);
					}
					break;
				}
			}else{
				if(cr.getRoomNum()==(roomNum)){
					rooms.remove(cr);
					rooms.add(r);
					if(cr.getLast()){
						r.setLast();
					}else{
						r.setNext(cr.getNextRoom());
						cr.setNext(null);
					}
					break;
				}
			}
		}
	}
	
	public int[] getDiff(){
		return new int[]{baseDiff,topDiff};
	}
	
	public int evalDiff(){
		baseDiff=0;
		topDiff=0;
		int temp;
		for(Room r:rooms){
			temp=r.evaluateDiff();
			if(temp>topDiff){
				topDiff=temp;
			}
			baseDiff+=temp;
		}
		return baseDiff;
	}

	/**
	 * adds the dungeon to the master dungeon list, and defines the last made room as the last room
	 * 
	 * @param p
	 */
	public void finalize(Player p){
		dungeons.add(this);
		p.sendMessage("You have created dungeon "+name+" with "+rooms.size()+" rooms!");
		last.setLast();
	}
	
	/**
	 * sets the current room being edited by a DM
	 * 
	 * @param roomNum
	 */
	public void setEditing(int roomNum){
		for(Room r:rooms){
			if(r.getRoomNum()==roomNum){
				this.editing=r;
				roomSelected=true;
			}
		}
		
	}
	
	/**
	 * returns whether or not a room has been selected
	 * 
	 * @return
	 */
	public boolean selectedRoom(){
		return roomSelected;
	}
	
	/**
	 * returns the currently selected room
	 * 
	 * @return
	 */
	public Room getSelectedRoom(){
		return editing;
	}
	
	/**
	 * Called when a RaidGroup finishes the final room
	 * 
	 * @param group
	 */
	public void finish(RaidGroup group){
		//leaveDungeon(group);
		float denom=(this.baseDiff)<=(group.getTopRating()*group.getGroup().size())?1+(Math.abs(group.getTopRating()*group.getGroup().size())/(this.baseDiff/this.roomCount)+1):group.getGroupRating()/group.getGroup().size();
		float ratingMod=(((this.baseDiff)-(group.getGroupRating()/2))>0?
				(this.topDiff): 0)/
				(denom!=0?denom:1);
		int increase=(ratingMod<1)?0:(int)ratingMod;
		group.increaseRating(increase>5?5:increase);
		group.tell(ChatColor.GREEN+"Congratulations on completing the dungeon "+this.name);
		group.leaveDungeon(false);
		//call looting
		group.dungeonComplete(this.topDiff);
	}

	/**
	 * returns the dungeons name
	 * 
	 * @return name
	 */
	public String getName(){
		return name;
	}

	/**
	 * Renames the dungeon
	 * 
	 * @param name
	 * @param p
	 */
	public void rename(String name,Player p){
		boolean fuckoff=false;
		for(Dungeon d:dungeons){//check if name taken
			if(d.getName().equals(name)){
				fuckoff=true;
			}
		}
		if(!fuckoff){//if the name is not taken then rename
			this.deleteFile();
			Dungeons.logToFile(p.getDisplayName()+" has renamed "+this.name+" to "+name);
			this.name=name;
			p.sendMessage("Dungeon renamed to: "+name);
		}else{//if the name was taken then tell the player
			p.sendMessage(name+" is already taken");
		}
	}

	/**
	 * deletes this dungeon
	 * 
	 */
	public void delete(){
		if(currentGroups.size()>0){
			for(RaidGroup rg:currentGroups){//remove any RaidGroups that are in the dungeon
				rg.leaveDungeon(true);
				rg.tell("The dungeon you were in was deleted");
			}
			dungeons.remove(this);//remove the dungeon from the list
		}else{
			dungeons.remove(this);
		}
		this.deleteFile();//delete it's corresponding file
	}

	/**
	 * removes a raidgroup from the dungeon
	 * 
	 * @param g
	 * @return
	 */
	public boolean leaveDungeon(RaidGroup g){

		currentGroups.remove(g);
		g.getRoom().removeGroup();

		return true;
	}

	/**
	 * returns the list of rooms in the dungeon
	 * 
	 * @return
	 */
	public ArrayList<Room> getRooms(){
		return rooms;
	}
	
	/**
	 * set the dungeon ready
	 * 
	 * @param ready
	 */
	public void setReady(boolean ready){
		this.ready=ready;
	}
	
	/**
	 * check if the dungeon is ready
	 * 
	 * @return
	 */
	public boolean getReady(){
		return ready;
	}

	/*******************************************************
	 * 
	 * BELOW ARE THE STATIC METHODS FOR DUNGEON CONTROL
	 * 
	 * 
	 *******************************************************/
	
	
	/**
	 * try to add a group to the dungeon, check 1st room occupancy, and dungeon ready state.
	 * 
	 * @param name
	 * @param g
	 * @return
	 */
	public static boolean groupAttempt(String name, RaidGroup g){
		boolean found=false;
		for(Dungeon d:dungeons){
			if(d.getName().equals(name)){//if the named dungeon exists
				found=true;
				if(!d.getReady()){//if it is ready
					g.tell("The Dungeon "+d.getName()+" is not ready!");
					return false;
				}
				for (Room r:d.rooms){//find the first room
					if(r.getRoomNum()==1){
						if(r.setGroup(g)){//try to add the group to that room
							g.setCurrentDungeon(d);
							d.currentGroups.add(g);
							return true;
						}else{//if fails then tell the group
							g.tell("Room 1 of that dungeon is currently occupied "+ name);
						}
						break;	
					}
				}
				break;
			}
		}

		if (!found){//identify if a dungeon with the given name does not exist
			g.tell("No such dungeon");
		}


		return false;
	}


	
	
	/**
	 * Get a dungeon based on a String name
	 * 
	 * @param name
	 * @return
	 */
	public static Dungeon getDungeon(String name){
		for(Dungeon d:dungeons){
			if(d.getName().equals(name)){
				return d; 
			}
		}
		return null;
	}

	/**
	 * get the list of dungeons and return it as a string
	 * 
	 * @return
	 */
	public static String getDungeonList(){
		String response=ChatColor.GREEN+"Dungeons: ";
		int amount=dungeons.size();
		for(Dungeon d:dungeons){//loop through the dungeons
			amount--;
			if(d.getReady()){//if the dungeon is ready then add it to the list in green
				if(amount>0){
					response=response+ChatColor.BLUE+d.getName()+ChatColor.GREEN+", ";
				}else{
					response=response+ChatColor.BLUE+d.getName();
				}
			}else{//if not ready add in red
				if(amount>0){
					response=response+ChatColor.RED+d.getName()+", ";
				}else{
					response=response+ChatColor.RED+d.getName();
				}
			}
		}

		return response;
	}

	/**
	 * Assign a dungeon to a DM player
	 * 
	 * @param p
	 * @return
	 */
	public static Dungeon assignDungeon(Player p){
		Date now=new Date();//get the current date and time
		for(Dungeon d:dungeons){//loop through dungeons looking for any unowned ones
			if(!d.owned){//if one is found
				d.assign(p);//then assign it
				
				return d;
			}		
		}
		
		for(Dungeon d:dungeons){//if none are unowned then look again for a dungeon whose owner has had it longer than guaranteed time
			if((d.owned &&(now.getTime()-d.getDate().getTime())>(Constants.ownerTime*Constants.HOUR))||d.owner.equals(p.getUniqueId().toString())){
				d.assign(p);//assign it
				return d;
			}
		}
		return null;
	}
	
	
	
	/**********************************
	 * 
	 * Following is IO for writing and
	 * Reading Dungeons
	 * 
	 *
	 **********************************/
	
	
	/**
	 * Save the dungeons to file
	 */
	public static void saveDungeons(){
		Dungeons.getPlugin(Dungeons.class).log("Saving Dungeons");
		dungeonNames=new ArrayList<String>();
		File dList=new File(Constants.path+Constants.DUNGEONLIST);
		for (Dungeon d:dungeons){//add dungeon names to the name list
			dungeonNames.add(d.getName());//add the name
			d.save();//tell dungeon to save itself
		}
		
		try {//save the list to file
			if(!dList.exists()){
				dList.createNewFile();
			}
			FileOutputStream f_out=new FileOutputStream(Constants.path+Constants.DUNGEONLIST);
			ObjectOutputStream obj_out=new ObjectOutputStream(f_out);
			obj_out.writeObject(dungeonNames);
			obj_out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException i) {
			// TODO Auto-generated catch block
			i.printStackTrace();
		}
	}
	
	/**
	 * load the dungeons from file
	 */
	public static void loadDungeons(){
		Dungeons.getPlugin(Dungeons.class).log("loading dungeons");
		File dList=new File(Constants.path+Constants.DUNGEONLIST);
		ObjectInputStream obj_in;
		FileInputStream f_in;
		Object obj;
		Dungeon d;
		try {
			if(dList.exists()){//get dungeon list from file
				f_in=new FileInputStream(Constants.path+Constants.DUNGEONLIST);
				obj_in=new ObjectInputStream(f_in);
				obj=obj_in.readObject();
				dungeonNames=(ArrayList<String>)obj;//set the dungeonnames from the dungeonlist file
				
				for(String s:dungeonNames){//loop through the dungeon names
					f_in=new FileInputStream(Constants.path+Constants.DUNGEONSAVE+s+".data");//load files for corresponding dungeons
					obj_in=new ObjectInputStream(f_in);
					obj=obj_in.readObject();//read the dungeon object in
					
					//confirm the object is actually an instance of Dungeon (stops attempting to load it if the file is fucked
					if(obj instanceof Dungeon){
						d=(Dungeon)obj;//cast the object read to file as dungeon
						d.load();//tell the dungeon to load itself  
						dungeons.add(d);//add the dunegon to the master ArrayList
					}
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException i) {
			// TODO Auto-generated catch block
			i.printStackTrace();
		} catch (ClassNotFoundException o) {
			// TODO Auto-generated catch block
			o.printStackTrace();
		}
	}
	
	/**
	 * individual dungeon saving to file
	 */
	public void save(){
		File dung=new File(Constants.path+Constants.DUNGEONSAVE+this.name+".data");//set filename and path
		Dungeons.getPlugin(Dungeons.class).log("Saving "+this.name);
		for(Room r:rooms){//tell rooms to prep for save
			r.prepForSave();
		}
		
		try {
			if(!dung.exists()){//check if the file already exists
				dung.getParentFile().mkdirs();//if not a new file is made
				dung.createNewFile();
			}
			
			//save the Dungeon object to file
			FileOutputStream f_out=new FileOutputStream(Constants.path+Constants.DUNGEONSAVE+this.name+".data");
			ObjectOutputStream obj_out=new ObjectOutputStream(f_out);
			obj_out.writeObject(this);
			obj_out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException i) {
			// TODO Auto-generated catch block
			i.printStackTrace();
		}
		
	}
	
	/**
	 * ready itself for use by telling it's rooms to load
	 * (some values in room are not serializable and are written to strings prior to save
	 * this tells the room to recreate the original objects)
	 */
	public void load(){
		Dungeons.getPlugin(Dungeons.class).log("loading: "+this.name);
		this.currentGroups=new ArrayList<RaidGroup>();
		
		roomSelected=false;
		for(Room r:rooms){
			r.load();
		}
		
		this.evalDiff();
	}
	
	/**
	 * deletes this dungeon's file if it exists
	 */
	private void deleteFile(){
		File dung=new File(Constants.path+Constants.DUNGEONSAVE+this.name+".data");
		if(dung.exists()){
			dung.delete();
		}
	}

}
