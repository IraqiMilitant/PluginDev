package com.khelm.dungeons.cmdexecutors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.khelm.dungeons.Constants;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.RaidGroup;
import com.khelm.dungeons.Raider;
/**
 * Command executor for all Group commands
 * 
 * @author IraqiMilitant
 *
 */
public class GroupCommandExecutor implements CommandExecutor {

	private final Dungeons plugin;
	private Map<Raider,RaidGroup> pendingInvites;

	public GroupCommandExecutor(Dungeons plugin){
		this.plugin=plugin;
		pendingInvites=new HashMap<Raider,RaidGroup>();
	}

	/**
	 * runs for every /group command
	 */
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Player p;
		ArrayList<String>players=new ArrayList<String>();
		RaidGroup rg;
		if (s instanceof Player){//confirm sender is player
			p=(Player)s;

		}else{
			s.sendMessage("You must be a player to run that command");
			return false;
		}
		Raider r=Raider.getRaider(p);
		if (args.length<1){
			if(!r.hasGroup()){//confirm player has a group
				s.sendMessage("/group form <player> <player> ...: make a new group\n/group invite <player>: invite people to your group\n/group leave: leave your current group");
				return true;
			}

			//message the player with their group info
			String response= ChatColor.GREEN+"Members:";

			for(Raider ra:r.getGroup().getGroup()){
				if(ra.getReady()){
					response=response +ChatColor.BLUE+" "+ra.getName();
				}else{
					response=response +ChatColor.RED+" "+ra.getName();
				}
			}

			p.sendMessage(ChatColor.GREEN+"-----------------[GROUP INFO]-----------------");
			p.sendMessage(ChatColor.GREEN+"Leader: "+ChatColor.YELLOW+r.getGroup().getLeader().getName());
			p.sendMessage(ChatColor.GREEN+"Your Group Rating is: "+ChatColor.YELLOW+r.getGroup().getGroupRating());
			p.sendMessage(response);

			return true;

		}else if(args[0].equals("form")){//create a new group and invite people
			if (!r.hasGroup()){//check if they already have a group
				rg=new RaidGroup(r);//create a new group
				r.setGroup(rg);
				if(args.length>2){//if there are extra args
					for(String st:args){//treat args as player names and invite those players to the group
						if(!args[0].equals(st)){
							if(!st.equals(p.getDisplayName())){
								players.add(st);
							}
						}
					}
					sendInvites(players,p,r.getGroup());
				}else if(args.length==2){
					if(!args[1].equals(p.getDisplayName())){
						sendInvite(args[1],p,r.getGroup());
					}
				}
				s.sendMessage(ChatColor.GREEN+"New Group Created!");
			}else{
				s.sendMessage(ChatColor.RED+"You are already in a group");
			}
			return true;

		}else if(args[0].equals("invite")){//invite players to group

			if (r.hasGroup()){//if sender has a group
				if(r.getGroup().getLeader()==r){//if they are the group lead send invite
					if(r.getGroup().getGroup().size()<Constants.maxGroupSize){
						if(!args[1].equals(p.getDisplayName())){


							s.sendMessage("Invite Sent to: "+args[1]);
							sendInvite(args[1],p,r.getGroup());
						}else{
							p.sendMessage(ChatColor.RED+"You are already in the group");
						}
					}else{
						s.sendMessage(ChatColor.RED+"Your group is full");
					}
				}else{
					s.sendMessage(ChatColor.RED+"You are not the group leader");
				}
			}else{//if they don't have a group then form 1 and send invite
				rg=new RaidGroup(r);
				r.setGroup(rg);
				sendInvite(args[1],p,r.getGroup());
				s.sendMessage(ChatColor.GREEN+"New Group Created");
			}
			return true;

		}else if(args[0].equals("leave")){//leave group
			if (r.hasGroup()){//if they have a group
				r.leaveGroup();//leave it
				s.sendMessage(ChatColor.RED+"You have left your group");
			}else{

				s.sendMessage(ChatColor.RED+"You are not in a group");
			}
			return true;

		}else if(args[0].equals("accept")){//accept invite
			if(pendingInvites.containsKey(r)&&!r.hasGroup()){//if there is a pending invite for the player
				if(pendingInvites.get(r).getGroup().size()<Constants.maxGroupSize){
					r.setGroup(pendingInvites.get(r));
					r.getGroup().addRaider(r);
					pendingInvites.remove(r);
					p.sendMessage(ChatColor.GREEN+"You have joined the group!!");
				}else{
					s.sendMessage(ChatColor.RED+"That group is now full");
				}
				return true;
			}else if(r.hasGroup()){
				p.sendMessage(ChatColor.RED+"You are already in a group");
			}
			p.sendMessage(ChatColor.RED+"You have no pending group invitations");
			return true;

		}else if(args[0].toLowerCase().equals("ready")){//ready commands
			if (args.length==2){
				if (args[1].toLowerCase().equals("set")){//toggle player ready
					if(r.hasGroup()){
						if(!r.getEdits()){
							r.setReady();
							if(r.getReady()){
								p.sendMessage(ChatColor.GREEN+"You have set yourself to ready ");
								r.getGroup().tell(ChatColor.GREEN+p.getDisplayName()+" is ready!");
							}else{
								p.sendMessage(ChatColor.RED+"You have set yourself to not-ready ");
								r.getGroup().tell(ChatColor.RED+p.getDisplayName()+" is no longer ready!");
							}
							return true;
						}else{
							p.sendMessage("You are currently editing a dungeon, and cannot ready-up");
						}
					}else{
						return false;
					}
				}else{
					p.sendMessage(ChatColor.RED+"You are not in a Group!");
				}
				return true;
			}else{//check group ready status

				if(r.hasGroup()){
					ArrayList<String>notReady=new ArrayList<String>();
					for(Raider ra:r.getGroup().getGroup()){
						if(!ra.getReady()){
							notReady.add(ra.getName());
						}
					}
					if(notReady.size()==0){
						p.sendMessage(ChatColor.GREEN+"Your group is ready!");

					}else{
						String response=ChatColor.RED+"Players not ready: ";
						for(String nr:notReady){
							response=response+nr+" ";
						}
						p.sendMessage(response);

					}
				}else{
					p.sendMessage(ChatColor.RED+"You are not in a group!");
				}
				return true;
			}

		}

		return false;
	}

	/**
	 * send invites to players
	 * 
	 * @param players
	 * @param sender
	 * @param group
	 */
	private void sendInvites(ArrayList<String> players, Player sender, RaidGroup group){
		ArrayList<String> cantFind=new ArrayList<String>();
		boolean found=false;
		Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
		for(String s:players){
			found=false;
			for (Player p : onlinePlayers){
				if(p.getName().equals(s)){
					p.sendMessage(ChatColor.GREEN+"You have been invited to a group by "+sender.getName()+" use "+ChatColor.BLUE+"/group accept"+ChatColor.GREEN+" to join");
					pendingInvites.put(Raider.getRaider(p), group);
					//invite
					found=true;
				}
			}
			if(!found){
				cantFind.add(s);
			}

		}
		if(cantFind.size()>0){
			sender.sendMessage("Player(s): "+cantFind.toString()+" could not be found!");
		}

	}

	/**
	 * send a single invite
	 */
	private void sendInvite(String player, Player sender,RaidGroup group){
		boolean found=false;
		Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
		for (Player p : onlinePlayers){
			if(p.getName().equals(player)){
				p.sendMessage(ChatColor.GREEN+"You have been invited to a group by "+sender.getName()+" use "+ChatColor.BLUE+"/group accept"+ChatColor.GREEN+" to join");
				pendingInvites.put(Raider.getRaider(p), group);
				//invite
				found=true;
			}
		}
		if(!found){
			sender.sendMessage(player+" could not be found!");
		}


	}

}
