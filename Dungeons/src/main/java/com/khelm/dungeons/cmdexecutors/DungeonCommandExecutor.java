package com.khelm.dungeons.cmdexecutors;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Dungeon;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;
import com.khelm.dungeons.Room;

/**
 * Command Executor for all /dungeon commands
 * 
 * @author IraqiMilitant
 *
 */
public class DungeonCommandExecutor implements CommandExecutor {

	private boolean inProgress=false;//is there a dungeon being built

	//flags for command behaviour
	private boolean waitingForCorner=false;
	private boolean waitingForEntry=false;
	private boolean waitingForExit=false;
	private boolean roomOrFinalize=false;
	private boolean editing=false;

	//who is building a dungeon
	private Player creator;
	private Location c1,c2,entry,exit;//locaiton values saved for room creation
	private int cCount=0;//flag for corner saving behaviour
	private int roomNum=0;//number of room being made
	private Dungeon dungeon;//dungeon being built
	private Room previousRoom,newRoom;//room tracking

	//private final Dungeons plugin;//instance of plugin

	private static DungeonCommandExecutor cmdExec;//this

	/**
	 * constructor saves instance of plugin class
	 * @param plugin
	 */
	public DungeonCommandExecutor(Dungeons plugin){
		//this.plugin=plugin;
		cmdExec=this;
	}

	/**
	 * called on all /dungeon commands
	 */
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Player p;
		if (s instanceof Player){//confirm CommandSender is a player
			p=(Player)s;

		}else{
			s.sendMessage("You must be a player to run that command");
			return true;
		}

		if (args.length<1){
			//print Group help
			s.sendMessage("/dungeon new <name> : make a new dungeon");
			return true;

		}else if(args[0].equals("new")){//make a new dungeon
			if(args.length==2){
				if(Dungeon.getDungeon(args[1]) instanceof Dungeon){
					p.sendMessage("A dungeon with that name already exists");
					return true;
				}
				editing=false;
				dungeonBuilder(p,args[1]);
				return true;
			}
			return false;

		}else if(args[0].equals("room")){//trigger creation of a new room
			if(p==creator && inProgress && roomOrFinalize){
				p.sendMessage("Select the first corner of the room");
				cCount=0;
				waitingForCorner=true;
				waitingForEntry=false;
				waitingForExit=false;
				roomOrFinalize=false;
			}

		}else if(args[0].equals("here")){//used for defining entrance and exit locations
			if(p==creator && (inProgress||editing)){
				if(waitingForEntry){//define entrance
					entry=p.getLocation();
					waitingForEntry=false;
					waitingForExit=true;
					p.sendMessage("Please go stand at the exit point and run /dungeon here");
				}else if(waitingForExit && !editing){//define exit for dungeon creation
					exit=p.getLocation();

					roomNum++;
					newRoom=new Room(c1,c2,entry,exit,dungeon);
					dungeon.newRoom(newRoom);
					if(roomNum>1){
						previousRoom.setNext(newRoom);
					}
					previousRoom=newRoom;

					waitingForExit=false;
					p.sendMessage("Do you want to finalize or make a new room?(/dungeon finalize or /dungeon room");
					roomOrFinalize=true;
				}else if(waitingForExit && editing){//define exit for room edits
					exit=p.getLocation();
					newRoom=new Room(c1,c2,entry,exit,dungeon);
					roomOrFinalize=true;
					waitingForExit=false;
					p.sendMessage("Please use /dungeon finalize to finalize your edit or /cancel to cancel your edit");

				}
				return true;
			}

		}else if(args[0].equals("finalize")){//finalize edits and creations
			if(p==creator && inProgress && roomOrFinalize){//finalize a dungeon
				p.sendMessage("Dungeon Creation Finalize");
				inProgress=false;
				//previousRoom.setLast();
				roomOrFinalize=false;
				dungeon.finalize(p);
				return true;
			}else if(p==creator && editing && roomOrFinalize){//finalize an edit
				dungeon.replaceRoom(newRoom, previousRoom.getRoomNum());
				editing=false;
				roomOrFinalize=false;
				p.sendMessage("Room edit finalized");
				return true;

			}
			return false;

		}else if(args[0].equals("cancel")){//cancel an edit
			if(editing){
				p.sendMessage("Room edit canceled");
				editing=false;
				roomOrFinalize=false;
			}
			return true;

		}else if(args[0].equals("attempt")){//group attempt a dungeon
			Raider r=Raider.getRaider(p);
			if(r.hasGroup()){
				if(args.length<2){//ensure enough args
					p.sendMessage("Usage: /dungeon attempt <name>");
					return true;
				}
				if(r.getGroup().getLeader()==r){//if the sender is group lead
					if(!r.getReady()){
						r.setReady();
					}
					if(r.getGroup().checkReady()){//if the group is ready
						if(!r.isInDungeon()){
							if(Dungeon.groupAttempt(args[1], Raider.getRaider(p).getGroup())){//attempt to start the dungeon
								p.sendMessage(ChatColor.GREEN+"Your group will attempt dungeon "+args[1]);
							}
						}else{
							p.sendMessage(ChatColor.RED+"Your group is already in a dungeon!");
						}

					}else{
						p.sendMessage(ChatColor.RED+"Your group is not ready");
					}
				}else{
					p.sendMessage(ChatColor.RED+"You are not your group leader");

				}

			}else{
				p.sendMessage(ChatColor.RED+"You don't have a raid group, form one with"+ChatColor.BLUE+" /group form");

			}
			return true;
		}else if(args[0].equals("leave")){//leave a dungeon
			Raider r=Raider.getRaider(p);
			if(r.hasGroup()){//if in a group
				if(r.getGroup().getLeader()==r){//if group lead
					if(Raider.getRaider(p).getGroup().leaveDungeon(true)){//try to leave the dungeon
						return true;
					}else{

					}
				}else{
					p.sendMessage(ChatColor.RED+"You are not the group leader");

				}

			}else{
				p.sendMessage(ChatColor.RED+"You are not in a group!");
			}
			return true;
		}else if(args[0].equals("edit")){//edit a room
			if (p.isOp()){//if sender is op
				if (args.length==3){//confirm args
					dungeon=Dungeon.getDungeon(args[1]);//attempt to get the dungeon with the defined name
					if(dungeon instanceof Dungeon){//did we get a dungeon?
						for(Room r:dungeon.getRooms()){//get the selected room num
							if(r.getRoomNum()==Integer.parseInt(args[2])){
								//set flags for editing
								previousRoom=r;
								waitingForCorner=true;
								editing=true;
								creator=p;
								cCount=0;
								p.sendMessage("Select the first corner of the room");
								return true;
							}

						}
						p.sendMessage(ChatColor.RED+"That room does not exist");
						return true;
					}else{
						p.sendMessage(ChatColor.RED+"No dungeon by that name!");
						return true;
					}

				}
			}else{
				p.sendMessage(ChatColor.RED+"You do not have permission to do that!");				
			}

			return false;
		}else if(args[0].equals("delete")){//delete a dungeon
			Dungeon d;
			if(p.isOp()){	
				if (args.length==2){//confirm args
					if(p.isOp()){//if sender is op
						d=Dungeon.getDungeon(args[1]);
						if(d instanceof Dungeon){//if that dungeon exists
							d.delete();//make it not exist
						}
					}else{
						p.sendMessage("You do not have permission to do that!");
					}
					return true;
				}
			}
			return false;
		}else if(args[0].equals("list")){//list existing dungeons
			p.sendMessage(Dungeon.getDungeonList());
			return true;

		}else if(args[0].equals("rename")){//rename a dungeon
			if(args.length==3 && p.isOp()){
				Dungeon d=Dungeon.getDungeon(args[1]);
				if(d instanceof Dungeon){//confirm the dungeon exists
					d.rename(args[2],p);//attempt to rename it
				}
				return true;
			}

		}else if(args[0].equals("invade")){
			if(p.getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
				if(Raider.getRaider(p).deQueue()){
					p.sendMessage(ChatColor.RED+"You are no longer queued for invasion");
				}else{
					if(Raider.getRaider(p).queueForInvade()){
						p.sendMessage(ChatColor.BLUE+"You are now queued for invasion!!");
					}else{
						p.sendMessage(ChatColor.RED+"You do not have a invasion token");
					}
				}
				return true;
			}
		}else if(args[0].equals("arbiter")){
			if(p.getWorld().equals(Bukkit.getWorld(Constants.dunegonW))){
				if(Raider.getRaider(p).deQueue()){
					
					p.sendMessage(ChatColor.RED+"You are no longer queued for invasion");
				}else{
					if(Raider.getRaider(p).queueForArbiter()){
						p.sendMessage(ChatColor.BLUE+"You are now queued to be an arbiter!!");
					}else{
						p.sendMessage(ChatColor.RED+"You do not have a invasion token");
					}
				}
				return true;
			}

		}else{
			Dungeon d=Dungeon.getDungeon(args[0]);
			if(d instanceof Dungeon){
				p.sendMessage(ChatColor.GREEN+"-----------------[DUNGEON INFO]-----------------");
				p.sendMessage(ChatColor.GREEN+"Name: "+ChatColor.YELLOW+d.getName());
				p.sendMessage(ChatColor.GREEN+"Designer: "+ChatColor.YELLOW+d.getOwner());
				p.sendMessage(ChatColor.GREEN+"Base Difficulty: "+ChatColor.YELLOW+d.getDiff()[0]+ChatColor.GREEN+" Top Diff: "+ChatColor.YELLOW+d.getDiff()[1]);
				p.sendMessage(ChatColor.GREEN+"Rooms: "+ChatColor.YELLOW+d.getRooms().size());
				if(d.getReady()){
					p.sendMessage(ChatColor.BLUE+"Dungeon is ready");
				}else{
					p.sendMessage(ChatColor.RED+"Dungeon is not ready");
				}
				p.sendMessage(ChatColor.GREEN+"------------------------------------------------");


			}else{
				p.sendMessage(ChatColor.BLUE+"No dungeon by that name");
			}

			return true;
		}


		return false;
	}

	/**
	 * used to start the dungeon creation process
	 * 
	 * @param p
	 * @param name
	 */
	private void dungeonBuilder(Player p, String name){
		if(p.isOp()){
			inProgress=true;
			dungeon=new Dungeon(name);
			creator=p;
			p.sendMessage("New dungeon with name: "+name+"\n Please select the first corner of the first room");
			waitingForCorner=true;
			roomNum=0;
			cCount=0;
		}else{
			p.sendMessage(ChatColor.RED+"You do not have permission to do this");
		}

	}

	/**
	 * used for selecting corners of rooms
	 * 
	 * @param l
	 * @param p
	 * @return
	 */
	public boolean blockSelect(Location l,Player p){
		if(waitingForCorner && p==creator){
			cCount++;
			if(cCount==1){
				c1=l;
				p.sendMessage("Now select the second corner of the room");
			}else{
				c2=l;
				p.sendMessage("Please go stand at the entry point and run /dungeon here");
				waitingForEntry=true;
				waitingForCorner=false;

			}
			return true;
		}
		return false;
	}

	/**
	 * returns the instance used for command execution
	 * 
	 * @return
	 */
	public static DungeonCommandExecutor getCommandExecutor(){
		return cmdExec;
	}

}
