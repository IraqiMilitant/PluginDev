package com.khelm.dungeons.cmdexecutors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.khelm.dungeons.DatabaseWorker;
import com.khelm.dungeons.Dungeons;
import com.khelm.dungeons.Raider;

/**
 * Command Executor for all /rating commands
 * 
 * @author IraqiMilitant
 *
 */
public class RatingCommandExecutor implements CommandExecutor {

	private final Dungeons plugin;

	public RatingCommandExecutor(Dungeons plugin){
		this.plugin=plugin;
	}

	/**
	 * runs every time a /rating command is sent
	 * 
	 */
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Player p=null;
		boolean isPlayer=false;
		if (s instanceof Player){//check if player
			p=(Player)s;
			isPlayer=true;
		}else{
			isPlayer=false;
		}

		if(args.length==0){
			if(!isPlayer){//if not a player
				s.sendMessage(ChatColor.GREEN+"/rating check <player>: to check a player's rating\n/rating set <global/local> <player> <value>: set player rating ");
				return true;
			}else{//display players rating
				p.sendMessage(ChatColor.GREEN+"Your Rating is: "+Raider.getRaider(p).getLocalRating());
				//p.sendMessage(ChatColor.GREEN+"Your Global Rating is: "+Raider.getRaider(p).getGlobalRating());
				return true;
			}

		}else if(args[0].equals("top")){
			final HashMap<String,Integer> ratings=DatabaseWorker.getPlayerRatings();
			List<String> players = new ArrayList<String>(ratings.keySet());
			Collections.sort(players, new Comparator<String>() {
			    public int compare(String s1, String s2) {
			        Integer rating1 = ratings.get(s1);
			        Integer rating2 = ratings.get(s2);
			        return rating1.compareTo(rating2);
			    }
			});
			p.sendMessage(ChatColor.GREEN+"Top 10 Ratings on this server!");
			for(int x=players.size()-1;x>(players.size()>10?players.size()-11:0);x--){
				p.sendMessage(ChatColor.YELLOW+players.get(x)+" : "+ratings.get(players.get(x)));
			}
			p.sendMessage(ChatColor.GREEN+"------------------------------");
			return true;
			
		}else if(args[0].equals("check")){//check a target players rating
			if(args.length<2){
				return false;
			}
			Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
			for (Player pl : onlinePlayers){//loop online players to check rating
				if(pl.getName().equals(args[2])){
					s.sendMessage(ChatColor.GREEN+args[2]+"'s Local Rating is: "+Raider.getRaider(pl).getLocalRating());
					s.sendMessage(ChatColor.GREEN+args[2]+"'s Global Rating is: "+Raider.getRaider(pl).getGlobalRating());
					return true;
				}
			}

			s.sendMessage(ChatColor.RED+"Player not found!");



		}else if(args[0].equals("set")){//set rating
			if ((isPlayer&&p.isOp())||!isPlayer){//only works if console or OP player
				if(args.length==4&&(args[1].equals("global")||args[1].equals("local"))){
					Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
					for (Player pl : onlinePlayers){//find target player
						if(pl.getName().equals(args[2])){
							//set rating
							if(args[1].equals("global")){
								Raider.getRaider(pl).setGlobalRating(Integer.parseInt(args[3]),false);
								s.sendMessage(ChatColor.GREEN+"Player's global rating set to "+args[3]);
							}else{
								Raider.getRaider(pl).setLocalRating(Integer.parseInt(args[3]),false);
								s.sendMessage(ChatColor.GREEN+"Player's local rating set to "+args[3]);
							}
							return true;
						}
					}

					s.sendMessage(ChatColor.RED+"Player not found!");
				}else{
					s.sendMessage("/rating set <global/local> <name> <rating>");
				}
			}

		}else if(args[0].equals("resetall")){//reset all ratings (wipe database tables)
			if (!isPlayer){
				DatabaseWorker.wipeTables();
				s.sendMessage(ChatColor.GREEN+"ALL TABLES RESET");
			}
		}
		return false;
	}

}
