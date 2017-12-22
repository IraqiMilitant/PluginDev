package com.khelm.dungeons;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.khelm.dungeons.cmdexecutors.DMCommandExecutor;
import com.khelm.dungeons.cmdexecutors.DungeonCommandExecutor;
import com.khelm.dungeons.cmdexecutors.GearCommandExecutor;
import com.khelm.dungeons.cmdexecutors.GroupCommandExecutor;
import com.khelm.dungeons.cmdexecutors.MobCommandExecutor;
import com.khelm.dungeons.cmdexecutors.PerkCommandExecutor;
import com.khelm.dungeons.cmdexecutors.RaiderCommandExecutor;
import com.khelm.dungeons.cmdexecutors.RatingCommandExecutor;
import com.khelm.dungeons.listeners.InventoryListener;
import com.khelm.dungeons.listeners.LoginListener;
import com.khelm.dungeons.listeners.MobListener;
import com.khelm.dungeons.listeners.PlayerListener;
import com.khelm.dungeons.listeners.WorldListener;
import com.khelm.dungeons.mobcontrol.MobGroup;
import com.khelm.dungeons.mobcontrol.MobGroupType;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
public final class Dungeons extends JavaPlugin {

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
		//load dungeons from file
		Dungeon.loadDungeons();
		//MobGroup.loadResGroups();


		//initialize the database and players
		initDB();
		initPlayers();
		//register listeners
		getServer().getPluginManager().registerEvents(new LoginListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new MobListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new WorldListener(), this);
		//register command executors 
		this.getCommand("group").setExecutor(new GroupCommandExecutor(this));
		this.getCommand("rating").setExecutor(new RatingCommandExecutor(this));
		this.getCommand("dungeon").setExecutor(new DungeonCommandExecutor(this));
		this.getCommand("dm").setExecutor(new DMCommandExecutor(this));
		this.getCommand("mob").setExecutor(new MobCommandExecutor(this));
		this.getCommand("raider").setExecutor(new RaiderCommandExecutor(this));
		this.getCommand("gear").setExecutor(new GearCommandExecutor(this));
		this.getCommand("perk").setExecutor(new PerkCommandExecutor(this));

		Constants.hasEco=setupEconomy();
		if(Constants.hasEco){
			Constants.eco=economy;
		}
		

	}

	@Override
	public void onDisable() {
		DatabaseWorker.disconnect();//terminate connection with database
		Dungeon.saveDungeons();//save dungeons to file
		//	MobGroup.saveResGroups();
		DMCommandExecutor.getExecutor().saveDMItems();
		logToFile("Dungeons disabled, logging terminated...");
		pw.close();
	}

	/**
	 * Creates Raider objects for all online players
	 */
	private void initPlayers(){
		Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
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
		Constants.user=getConfig().getString("user");
		Constants.pass=getConfig().getString("pass");
		Constants.host=getConfig().getString("host");
		Constants.dbName=getConfig().getString("dbName");
		Constants.port=getConfig().getString("port");
		Constants.serverName=(getConfig().getString("serverName")).toUpperCase();
		Constants.welcome=getConfig().getBoolean("sendWelcome");
		Constants.cashReward=getConfig().getInt("cashReward");

		Constants.maxGroupSize=getConfig().getInt("maxGroupSize");

		Constants.dunegonW=getConfig().getString("dungeonW");

		Constants.exitX=getConfig().getDouble("exitX");
		Constants.exitY=getConfig().getDouble("exitY");
		Constants.exitZ=getConfig().getDouble("exitZ");

		Constants.exit=new Location(Bukkit.getWorld(Constants.dunegonW),Constants.exitX,Constants.exitY,Constants.exitZ);

		Constants.resX=getConfig().getDouble("resX");
		Constants.resY=getConfig().getDouble("resY");
		Constants.resZ=getConfig().getDouble("resZ");

		Constants.res=new Location(Bukkit.getWorld(Constants.dunegonW),Constants.resX,Constants.resY,Constants.resZ);

		Constants.ownerTime=getConfig().getInt("ownerTime");

		Constants.marker=getConfig().getInt("marker");

		Constants.themes=new ArrayList<Theme>();
		Constants.path=this.getDataFolder().getAbsolutePath();

		Constants.trollshawn=getConfig().getBoolean("trollshawn");

		setThemes(getConfig().getConfigurationSection("themes"));
		setMobGroups(getConfig().getConfigurationSection("mobgroups"));
		setEffectValues(getConfig().getConfigurationSection("effects"));
		setHazardValues(getConfig().getConfigurationSection("hazards"));
		Constants.commands=(ArrayList<String>)getConfig().getStringList("cmdwhitelist");
		setAbilityValues();
		loadLootTables();
	}



	/**
	 * reads in loot table values from config
	 * 
	 */
	private void loadLootTables(){
		Constants.weapon_Melee=new HashMap<String,Integer>();
		Constants.weapon_Ranged=new HashMap<String,Integer>();
		Constants.armour=new HashMap<String,Integer>();
		Constants.enchant_Melee=new HashMap<String,Integer>();
		Constants.enchant_Ranged=new HashMap<String,Integer>();
		Constants.enchant_Armour=new HashMap<String,Integer>();
		Constants.potion_Normal=new HashMap<String,Integer>();
		Constants.potion_Splash=new HashMap<String,Integer>();
		Constants.item_Misc=new HashMap<String,Integer>();
		Constants.unique_Armour=new HashMap<String,Integer>();

		ConfigurationSection c;
		String path;

		this.log("melee weapons");
		path="loottable.weapons.melee";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.weapon_Melee.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("ranged weapons");
		path="loottable.weapons.ranged";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.weapon_Ranged.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("armour");
		path="loottable.armour";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.armour.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("melee enchants");
		path="loottable.enchants.weapon.melee";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.enchant_Melee.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("ranged enchants");
		path="loottable.enchants.weapon.ranged";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.enchant_Ranged.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("armour enchants");
		path="loottable.enchants.armour";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.enchant_Armour.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("ptions");
		path="loottable.potions.normal";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.potion_Normal.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("splash potions");
		path="loottable.potions.splash";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.potion_Splash.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("misc");
		path="loottable.misc";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.item_Misc.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}

		this.log("armoursets");
		path="loottable.armoursets";
		c=getConfig().getConfigurationSection(path);
		for(String s:c.getKeys(false)){
			Constants.unique_Armour.put(s,getConfig().getInt(path+"."+s));
			this.log(s+":"+getConfig().getInt(path+"."+s));
		}
	}
	/**
	 * sets difficulty modifier for hazards
	 * 
	 * @param c
	 */
	private void setHazardValues(ConfigurationSection c){
		Constants.Hazards=new HashMap<String,Integer>();
		for(String s:c.getKeys(false)){
			Constants.Hazards.put(s, getConfig().getInt("hazards."+s));
		}
	}
	/**
	 * sets the mob abilities/effects difficulty values based on the config
	 */
	private void setAbilityValues(){
		Constants.abilities=new HashMap<String,Integer>();
		Constants.groupAbilities=new HashMap<String,Integer>();
		Constants.mobEffects=new HashMap<String,Integer>();

		ConfigurationSection c=getConfig().getConfigurationSection("mobabilities.abilities");
		for(String s:c.getKeys(false)){
			Constants.abilities.put(s, getConfig().getInt("mobabilities.abilities."+s));
		}

		c=getConfig().getConfigurationSection("mobabilities.groupabilities");
		for(String s:c.getKeys(false)){
			Constants.groupAbilities.put(s, getConfig().getInt("mobabilities.groupabilities."+s));
		}

		c=getConfig().getConfigurationSection("mobabilities.potioneffects");
		for(String s:c.getKeys(false)){
			Constants.mobEffects.put(s, getConfig().getInt("mobabilities.potioneffects."+s));
		}


	}

	/**
	 * sets diff values for effects given to players in specific rooms
	 * @param c
	 */
	private void setEffectValues(ConfigurationSection c){
		Constants.effects=new HashMap<String,Integer>();
		for(String s:c.getKeys(false)){
			Constants.effects.put(s, getConfig().getInt("effects."+s));
		}
	}

	/**
	 * read in the defined mob groups
	 * 
	 * @param c
	 */
	private void setMobGroups(ConfigurationSection c){
		ArrayList<String>mobs=new ArrayList<String>();
		for(String s:c.getKeys(false)){
			mobs=(ArrayList<String>)getConfig().getStringList("mobgroups."+s+".mobs");
			new MobGroupType(s,mobs,getConfig().getInt("mobgroups."+s+".basediff"));
		}
	}
	/**
	 * creates the theme objects, as defined in the config
	 * 
	 * @param c
	 */
	private void setThemes(ConfigurationSection c){
		ArrayList<ThemeBlock> otherBlocks;
		ArrayList<ThemeBlock> floorBlocks;
		ConfigurationSection cs;
		ConfigurationSection bs;
		String gen="";
		boolean torches=false;
		String[] blockStr;
		for(String s:c.getKeys(false)){//loop theme names
			cs=getConfig().getConfigurationSection("themes."+s);
			otherBlocks=new ArrayList<ThemeBlock>();
			floorBlocks=new ArrayList<ThemeBlock>();
			for(String a:cs.getKeys(false)){//loop theme sub-keys
				if(a.equals("generation")){//grab the generation mode
					gen=getConfig().getString("themes."+s+"."+a);
					continue;
				}else if(a.equals("torches")){//check whether or not to add torches to generation
					if(getConfig().getString("themes."+s+"."+a).equals("true")){
						torches=true;
					}else{
						torches=false;
					}
					continue;
				}
				bs=getConfig().getConfigurationSection("themes."+s+"."+a);
				log("themes."+s+"."+a);
				for(String b:bs.getKeys(false)){//loop blocks in the theme

					blockStr=bs.getString(b).split(":");//get the strings defining each block
					if(a.equals("other")){//check if it is in the floor section or other
						if(blockStr[0].equals("0")){//save blocks to the theme
							otherBlocks.add(new ThemeBlock(Integer.parseInt(b),false,Integer.parseInt(blockStr[1])));
						}else{
							otherBlocks.add(new ThemeBlock(Integer.parseInt(b),true,Integer.parseInt(blockStr[1])));
						}
					}else{
						if(blockStr[0].equals("0")){
							floorBlocks.add(new ThemeBlock(Integer.parseInt(b),false,Integer.parseInt(blockStr[1])));
						}else{
							floorBlocks.add(new ThemeBlock(Integer.parseInt(b),true,Integer.parseInt(blockStr[1])));
						}
					}
				}
			}
			if(!(Theme.getTheme(s) instanceof Theme)){//check if this theme already exists
				Theme.addTheme(new Theme(s,otherBlocks,floorBlocks,gen,torches));//add theme to list
			}

		}
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
			File dataFolder=new File(Dungeons.getPlugin(Dungeons.class).getDataFolder().toString(),File.separator+"DungeonLogs");
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

	/*
	 * 
	 * VAULT INTEGRATION
	 * 
	 * 
	 */

	public static Economy economy = null;

	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}


}
