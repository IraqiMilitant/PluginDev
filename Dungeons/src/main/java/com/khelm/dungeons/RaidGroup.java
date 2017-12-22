package com.khelm.dungeons;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *Class which tracks group data (group rating, raiders in the group, and current dungeon etc)
 * 
 * @author IraqiMilitant
 *
 */
public class RaidGroup {
	//all existing groups
	private static ArrayList<RaidGroup>groups=new ArrayList<RaidGroup>();

	//Raiders in the group
	private ArrayList<Raider>group;
	//group leader
	private Raider leader;
	//current group rating
	private int groupRating;
	//rating of top group member
	private int topRating;
	//groups current dungeon
	private Dungeon currentDungeon;
	//current room of the dungeon
	private Room currentRoom;
	//is the group in a dungeon
	private boolean inDungeon;
	
	private boolean beenInvaded;
	private boolean invader;
	private int invadeRating;
	
	private int dead;

	/**
	 * Raidgroup constructor assigns leader and inits the list of members
	 * 
	 * @param leader
	 */
	public RaidGroup(Raider leader){
		group=new ArrayList<Raider>();
		group.add(leader);
		this.groupRating=leader.getLocalRating();
		groups.add(this);
		this.leader=leader;
		inDungeon=false;
		dead=0;
	}

	public int getTopRating(){
		return topRating;
	}
	
	/**
	 * get the current group rating
	 * 
	 * @return
	 */
	public int getGroupRating(){
		return groupRating;
	}
	
	public boolean getInvader(){
		return invader;
	}
	
	public void setInvader(boolean i,boolean arbiter){
		invader=i;
		if(invader && !arbiter){
			beenInvaded=true;
		}
	}
	
	/**
	 * reacts to dungeon completion
	 * 
	 * @param dungeonRating
	 */
	public void dungeonComplete(int dungeonRating){
		for(Raider r:group){
			Dungeons.getPlugin(Dungeons.class).log("Raider loot: "+r.getName());
			r.generateLoot(beenInvaded?dungeonRating+(invadeRating/2):dungeonRating,false);

		}
	}

	/**
	 * get the current group leader
	 * 
	 * @return
	 */
	public Raider getLeader(){
		return leader;
	}
	
	/**
	 * Get the current room of the dungeon that the group is attempting
	 * 
	 * @return
	 */
	public Room getRoom(){
		return currentRoom;
	}
	
	public void increaseRating(int increase){
		for(Raider r:group){
			Dungeons.getPlugin(Dungeons.class).log("Raider rating: "+r.getName());
			r.setLocalRating(r.getLocalRating()+increase, false);
		}
	}
	
	/**
	 * reacts to death of a group member
	 * 
	 * @param p
	 */
	public void notifyDeath(Player p){
		this.dead=0;
		
		for(Raider r:group){
			if(r.getDead()){
				dead++;
			}
		}
		
		if(dead>=group.size()&&this.inDungeon){
			this.tell(ChatColor.RED+"Your entire group has perished, Dungeon failed");
			this.currentRoom.groupDeath();
			this.leaveDungeon(false);
		}else if(!this.inDungeon){
		}else{
			p.sendMessage(ChatColor.RED+"YOU DIED!!"+ChatColor.BLUE+" if your group makes it to the next room");
			p.sendMessage(ChatColor.BLUE+"you will rejoin them.");
		}
		
		if(Raider.getRaider(p).equals(this.leader)&& this.inDungeon){
			for(Raider r:group){
				if(!r.getDead()){
					this.leader=r;
					return;
					
				}
			}
		}
	}
	
	public int getDead(){
		return dead;
	}
	
	public void notifyRes(){
		this.dead--;
	}
	
	public void newRoom(){
		this.dead=0;
	}
	
	/**
	 * set the current room
	 * 
	 * @param r
	 */
	public void setRoom(Room r){
		currentRoom=r;
		this.dead=0;
	}

	/**
	 * set the group leader
	 * 
	 * @param leader
	 */
	public void setLeader(Raider leader){
		this.leader=leader;
	}

	/**
	 * get the list of group members
	 * 
	 * @return
	 */
	public ArrayList<Raider> getGroup(){
		return group;
	}

	/**
	 * get the current dungeon the group is in
	 * 
	 * @return
	 */
	public Dungeon getCurrentDungeon(){
		return currentDungeon;
	}
	
	/**
	 * check if the group is ready
	 * 
	 * @return
	 */
	public boolean checkReady(){
		for(Raider r:group){
			if(!r.getReady()||this.dead>0){
				return false;
			}
		}
		return true;
	}

	/**
	 * Set the current dungeon for the group
	 * 
	 * @param dungeon
	 */
	public void setCurrentDungeon(Dungeon dungeon){
		this.invader=false;
		this.beenInvaded=false;
		for (Raider m:group){
			m.enterDungeon();
		}
		currentDungeon=dungeon;
		inDungeon=true;
	}

	/**
	 * remove a raider from the group
	 * 
	 * @param r
	 */
	public void leaveGroup(Raider r){
		if(this.inDungeon){
			this.leaveDungeon(true);
		}
		if(group.size()==1){//if only member of the group then delete it
			groups.remove(this);
		}else{
			if (leader==r){//if raider is group lead then assign another
				group.remove(r);
				leader=group.get(0);
			}else{
				group.remove(r);
			}
			this.groupRating-=r.getLocalRating();//change group rating accordingly
		}
		
	}

	/**
	 * remove the group from current dungeon, if in one
	 * 
	 * @return
	 */
	public boolean leaveDungeon(boolean msg){
		if (inDungeon){
			if(currentDungeon.leaveDungeon(this)){
				if(msg){
					this.tell("leaving dungeon");
				}
				moveBack();
				this.currentRoom.clearRoom(false);
				inDungeon=false;
				dead=0;
				for(Raider r:group){
					if(r.getPlayer().isDead()){
						dead++;
					}
				}
				return true;
			}
		}
		this.tell("not in a dungeon so cannot leave");
		return false;
	}

	/**
	 * Add a raider to the group
	 * 
	 * @param r
	 */
	public void addRaider(Raider r){
		if (group.size()<Constants.maxGroupSize){
			group.add(r);
			this.groupRating+=r.getLocalRating();
			for (Raider m:group){//tell the group who joined
				if (m!=r){
					m.getPlayer().sendMessage(r.getName()+" has joined the group!");
				}
			}
		}
	}
	
	/**
	 * Update the group rating 
	 */
	public void updateRating(){
		groupRating=0;
		topRating=0;
		for(Raider r:group){
			groupRating+=r.getLocalRating();
			if(r.getLocalRating()>topRating){
				topRating=r.getLocalRating();
			}
		}
	}

	/**
	 * broadcast a message to the group
	 * 
	 * @param msg
	 */
	public void tell(String msg){
		for (Raider m:group){
			m.getPlayer().sendMessage(msg);
		}
	}

	/**
	 * move all members of the group to a location
	 * 
	 * @param l
	 */
	public void moveGroup(Location l){
		for (Raider m:group){
//			if(firstRoom){
//				m.enterDungeon();
//				m.getPlayer().sendMessage("Teleporting now");
//			}
			if(!(m.getPlayer().isDead())&&m.getDead()){
				m.setDead(false);
			}
			m.getPlayer().teleport(l);
			m.setReady();
			
		}
	}

	/**
	 * make the members of the group leave the dungeon (Raider exitDungeon() handles the actual movement of the player)
	 */
	public void moveBack(){
		for (Raider m:group){
			m.exitDungeon();
			m.updateArmour();
		}
	}

}
