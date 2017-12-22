package com.iraqimilitant.dungeonslink;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Utility {
	
	
	public static void welcome(Player p){
		final Player pl=p;
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonsLink.getPlugin(DungeonsLink.class), new Runnable(){

			public void run() {
				pl.sendMessage(ChatColor.GREEN+"Welcome new Dungeon Raider, and potential Dungeon Master");
				pl.sendMessage(ChatColor.GREEN+"This server hosts a set of custom content which allows our");
				pl.sendMessage(ChatColor.GREEN+"users to customize and challenge instanced dungeons");
				pl.sendMessage(ChatColor.GREEN+"for increasing rewards and prestige.");
				pl.sendMessage(ChatColor.GREEN+"You can ask a member of staff or check our forums");
				pl.sendMessage(ChatColor.GREEN+"if you have any questions,");
				pl.sendMessage(ChatColor.GREEN+"and remember all the commands you will need to participate");
				pl.sendMessage(ChatColor.GREEN+"can be found in our menu using "+ChatColor.BLUE+"/brierie"+ChatColor.GREEN+" .");
				pl.sendMessage(ChatColor.GREEN+"Please enjoy your time on Brierie Servers!");
				
			}
			
		},60L);
		
	}

    /**
     * Remove colour chars from a line of text that are used to hide it, and
     * stop when reaching clear-text
     *
     * @param line The line to unpack
     * @return The unpacked line
     */
    public static String unpackHiddenText(String line) {
        StringBuilder sb = new StringBuilder();
        boolean expectColourChar = true;
        boolean clearText = false;
        for (char c : line.toCharArray()) {
            if (clearText) {
                sb.append(c);
            } else {
                if (expectColourChar) {
                    if (c != ChatColor.COLOR_CHAR) {
                        // Since there is no colour char, we must have reached 
                        // the end of the hidden section
                        sb.append(c);
                        clearText = true;
                    }
                } else {
                    sb.append(c);
                }
                expectColourChar = !expectColourChar;
            }
        }
        return sb.toString();
    }
    

    /**
     * Add a colour char to the front of each character in order to hide it
     *
     * @param line The line to pack
     * @return The packed line
     */
    public static String packHiddenText(String line) {
        StringBuilder sb = new StringBuilder();
        // Place a color char in front of each char in order to hide the comments
        for (char c : line.toCharArray()) {
            sb.append(ChatColor.COLOR_CHAR).append(c);
        }
        return sb.toString();
    }
}
