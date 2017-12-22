package com.khelm.dungeons.cmdexecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;

public class RaiderCommandExecutor implements CommandExecutor {


	private static RaiderCommandExecutor cmdExec;//this
	
	public RaiderCommandExecutor(Dungeons plugin){
//		this.plugin=plugin;
		cmdExec=this;
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Raider r;
		Player p;
		if(args.length>0){
			p=Bukkit.getPlayer(args[0]);
			if(p instanceof Player){
				r=Raider.getRaider(p);
				
			}else{
				s.sendMessage(ChatColor.RED+"No player by that name");
				return true;
			}
			
		}else{
			p=(Player)s;
			r=Raider.getRaider(p);
		}
		
		if(r instanceof Raider){
			s.sendMessage(ChatColor.GREEN+"-----------[PLAYER INFO]-----------");
			s.sendMessage(ChatColor.GREEN+"Name: "+ChatColor.YELLOW+p.getDisplayName());
			s.sendMessage(ChatColor.GREEN+"RATING:");
			//s.sendMessage(ChatColor.GREEN+"    Global: "+ChatColor.YELLOW+r.getGlobalRating() );
			s.sendMessage(ChatColor.GREEN+"    Local: "+ChatColor.YELLOW+r.getLocalRating());
			s.sendMessage(ChatColor.GREEN+"Sin: "+ChatColor.YELLOW+r.getSin());
			s.sendMessage(ChatColor.GREEN+"Active Perk: "+ChatColor.YELLOW+r.getPerk().getName());
			s.sendMessage(ChatColor.GREEN+"Has Group: "+ChatColor.YELLOW+r.hasGroup());
		}
		return true;
	}
	
	/**
	 * returns the instance used for command execution
	 * 
	 * @return
	 */
	public static RaiderCommandExecutor getCommandExecutor(){
		return cmdExec;
	}
}
