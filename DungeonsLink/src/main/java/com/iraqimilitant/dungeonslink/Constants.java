package com.iraqimilitant.dungeonslink;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
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
	
	/*
	 * some basic time constants
	 */
	public static final int SECOND = 1000;
	public static final int MINUTE = 60 * SECOND;
	public static final int HOUR = 60 * MINUTE;
	public static final int DAY = 24 * HOUR;
	
	
	public static ArrayList<DungeonPerks> perks;
	
}
