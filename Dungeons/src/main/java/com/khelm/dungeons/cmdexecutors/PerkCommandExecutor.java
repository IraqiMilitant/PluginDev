package com.khelm.dungeons.cmdexecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;

public class PerkCommandExecutor implements CommandExecutor {


	private static PerkCommandExecutor cmdExec;//this
	
	public PerkCommandExecutor(Dungeons plugin){
//		this.plugin=plugin;
		cmdExec=this;
	}
	
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Raider r;
		Player p;
		if(s instanceof Player){
			p=(Player)s;
			r=Raider.getRaider(p);
		}else{
			s.sendMessage(ChatColor.RED+"You must be a player to run that command");
			return true;
		}
		if(args.length>0){
			if(args[0].toLowerCase().equals("toggle")){
				r.togglePerk(args[1]);
				return true;
			}
			
			
		}
		return false;
	}
	
	/**
	 * returns the instance used for command execution
	 * 
	 * @return
	 */
	public static PerkCommandExecutor getCommandExecutor(){
		return cmdExec;
	}
}
