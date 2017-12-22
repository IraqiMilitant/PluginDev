package com.khelm.dungeons;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;

import net.milkbowl.vault.economy.Economy;
/**
 * Class to hold hard-coded constants and values read from config
 * 
 * @author IraqiMilitant
 *
 */
public class Constants {
	/*
	 * Following are pulled from config for connecting to the database
	 */
	public static String host="";
	public static String user="";
	public static String pass="";
	public static String dbName="";
	public static String port="";
	
	public static boolean welcome=false;
	
	//name of the server, pulled from the config
	public static String serverName="";
	
	//max group size for attempting any dungeons, pulled from config
	public static int maxGroupSize=0;
	
	public static int cashReward=0;
	
	/*
	 * Following are pulled from config and define the exit point (where players go after leaving a dungeon)
	 */
	public static double exitX=0;
	public static double exitY=0;
	public static double exitZ=0;
	
	public static double resX=0;
	public static double resY=0;
	public static double resZ=0;
	
	//world the dungeons are in
	public static String dunegonW="";
	
	/*
	 * actual location objects generated from config values
	 */
	public static Location exit=null;
	
	public static Location res=null;
	
	//time a dungeon is guaranteed to be left alone after a DM configs it 
	public static int ownerTime=0;
	
	public static int marker=0; 
	
	/*
	 * some basic time constants
	 */
	public static final int SECOND = 1000;
	public static final int MINUTE = 60 * SECOND;
	public static final int HOUR = 60 * MINUTE;
	public static final int DAY = 24 * HOUR;
	
	//path variables for the dungeon list, and dungeon data files
	public static String path="";
	public static final String DUNGEONLIST="/dungeonList.data";
	public static final String DUNGEONSAVE="/dungeons/";
	public static final String DMITEMS="/DMItems.data";
	public static final String MOBGROUPS="/MobGroups.data";
	
	//list of defined themes
	public static ArrayList<Theme>themes;
	
	//maps for assigned difficulty values
	public static HashMap<String,Integer>effects;//effects applied to players
	
	//mob related
	public static HashMap<String,Integer>abilities;
	public static HashMap<String,Integer>groupAbilities;
	public static HashMap<String,Integer>mobEffects;
	
	//hazards
	public static HashMap<String,Integer>Hazards;
	
	//most important setting
	public static boolean trollshawn=true;
	
	//commands permitted in the dungeon world when in a dungeon or editing etc
	public static ArrayList<String>commands;
	
	public static Economy eco;
	public static boolean hasEco;
	
	
	/*
	 * 
	 * LOOT TABLES BELOW
	 * 
	 * 
	 */
	public static HashMap<String,Integer>weapon_Melee;
	public static HashMap<String,Integer>weapon_Ranged;
	public static HashMap<String,Integer>armour;
	public static HashMap<String,Integer>potion_Normal;
	public static HashMap<String,Integer>potion_Splash;
	public static HashMap<String,Integer>enchant_Melee;
	public static HashMap<String,Integer>enchant_Ranged;
	public static HashMap<String,Integer>enchant_Armour;
	public static HashMap<String,Integer>item_Misc;
	
	
	public static HashMap<String,Integer>unique_Armour;
	
	
}
