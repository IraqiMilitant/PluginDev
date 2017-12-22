package com.iraqimilitant.dungeonslink;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.iraqimilitant.dungeonslink.cmdexecutors.PerkCommandExecutor;
import com.iraqimilitant.dungeonslink.cmdexecutors.RaiderCommandExecutor;
import com.iraqimilitant.dungeonslink.listeners.LoginListener;
import com.iraqimilitant.dungeonslink.listeners.MobListener;
import com.iraqimilitant.dungeonslink.listeners.PlayerListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Top level plugin class, handles enabling and disabling the plugin
 * 
 * @author IraqiMilitant
 *
 */
public final class DungeonsLink extends JavaPlugin {

	private static File logFile;
	private static FileWriter fw;
	private static PrintWriter pw;


	@Override
	public void onEnable() {
		//set/get the config and pull config values
		getConfig().options().copyDefaults(true);
		saveConfig();
		setConstants();
		logToFile("Dungeons Enabled, File logging ready...");


		//initialize the database and players
		initDB();
		initPlayers();
		//register listeners
		getServer().getPluginManager().registerEvents(new LoginListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new MobListener(), this);
		//register command executors 
		this.getCommand("raider").setExecutor(new RaiderCommandExecutor(this));
		this.getCommand("perk").setExecutor(new PerkCommandExecutor(this));

	}

	@Override
	public void onDisable() {
		DatabaseWorker.disconnect();//terminate connection with database
		//	MobGroup.saveResGroups();
		logToFile("Dungeons disabled, logging terminated...");
		pw.close();
	}

	/**
	 * Creates Raider objects for all online players
	 */
	private void initPlayers(){
		Player[] onlinePlayers = getServer().getOnlinePlayers();
		for (Player player : onlinePlayers){
			DatabaseWorker.readPlayerInfo(player);
		}
	}

	/**
	 * prints a message to the console (log)
	 * 
	 * @param msg
	 */
	public void log(String msg){
		getLogger().info(msg);
	}

	/**
	 * prints a message to the console as a warning
	 * 
	 * @param msg
	 */
	public void warn(String msg){
		getLogger().warning(msg);
	}

	/**
	 * gets all the constant values from the config
	 */
	private void setConstants(){
		Constants.pass=getConfig().getString("pass");
		Constants.host=getConfig().getString("host");
		Constants.user=getConfig().getString("user");
		Constants.dbName=getConfig().getString("dbName");
		Constants.port=getConfig().getString("port");
		Constants.serverName=(getConfig().getString("serverName")).toUpperCase();
		Constants.welcome=getConfig().getBoolean("sendWelcome");
	}


	/**
	 * ready the database
	 */
	private void initDB(){
		if (!Constants.serverName.equals("temp")){//check if the database config values have been set
			DatabaseWorker.createTables();
			log("Database ready");
		}else{//if not warn user through the console
			warn("Set servername in the config and reboot the server to initialize the database");
		}
	}

	public static void logToFile(String msg){

		try {
			NumberFormat myFormat=NumberFormat.getInstance();
			myFormat.setMinimumIntegerDigits(2);
			Calendar now=Calendar.getInstance();
			String date=myFormat.format(now.get(Calendar.DAY_OF_MONTH))+" - "+myFormat.format(now.get(Calendar.MONTH)+1)+" - "+now.get(Calendar.YEAR);
			String time="["+myFormat.format(now.get(Calendar.HOUR_OF_DAY))+":"+myFormat.format(now.get(Calendar.MINUTE))+":"+myFormat.format(now.get(Calendar.SECOND))+"] - ";
			File dataFolder=new File(DungeonsLink.getPlugin(DungeonsLink.class).getDataFolder().toString(),File.separator+"DungeonLogs");
			if(!dataFolder.exists()){
				dataFolder.mkdir();
			}
			if(!(logFile instanceof File)){
			logFile=new File(dataFolder,date+".log");
			if(!logFile.exists()){

				logFile.createNewFile();

			}
			
			
			fw=new FileWriter(logFile,true);
			pw=new PrintWriter(fw);
			}
			
			pw.println(time+msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


}
