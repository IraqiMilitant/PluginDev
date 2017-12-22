package com.khelm.dungeons;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.khelm.dungeons.Constants;

import java.sql.ResultSet;

/**
 * Worker class which handles all interaction with the database
 * 
 * @author IraqiMilitant
 *
 */
public class DatabaseWorker {
	static Connection con;
	
	
	/**
	 * gets the connection to the database
	 * 
	 * @return Connection
	 */
	public static Connection connect(){

		try {
			if(!(con instanceof Connection) || !con.isValid(5)){//attempt connection if there is none
				Class.forName("com.mysql.jdbc.Driver");//set the driver
				Dungeons.getPlugin(Dungeons.class).log("attempting connection to jdbc:mysql://"+Constants.host+":"+Constants.port+"/"+Constants.dbName+" With Username: "+Constants.user+" and Pass: "+Constants.pass);
				if (Constants.pass.equals("nopass")){//check if there is a password
					con=DriverManager.getConnection("jdbc:mysql://"+Constants.host+":"+Constants.port+"/"+Constants.dbName);
					
				}else{
					con=DriverManager.getConnection("jdbc:mysql://"+Constants.host+":"+Constants.port+"/"+Constants.dbName,Constants.user,Constants.pass);	
				}
				Dungeons.getPlugin(Dungeons.class).log("Connection to SQL database established");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}

	/**
	 * Closes the connection to the database
	 */
	public static void disconnect(){

		try {
			if (con!=null && !con.isClosed()){//if there is a connection
				con.close();//close it
				Dungeons.getPlugin(Dungeons.class).log("SQL Database connection closed");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static HashMap<String,Integer> getPlayerRatings(){
		HashMap<String,Integer> ratings=new HashMap<String,Integer>();
		Statement stmt = null;
		ResultSet rs;
		String queryL=
				"SELECT *"
						+ " FROM " + Constants.serverName 
						+ "_USERS;";
		if(connect() instanceof Connection){
		try {
			stmt=con.createStatement();
			rs=stmt.executeQuery(queryL);//execute the Local query
			OfflinePlayer op;
			while (rs.next()){
				op=Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("UUID")));
				ratings.put(op.getName(), rs.getInt("RATING"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		return ratings;
	}
	
	public static boolean login(Raider r){
		Statement s;
		
		String date="UPDATE "
				+ "USERS"
				+ " SET LASTSEEN = '" + new java.sql.Date(Calendar.getInstance().getTimeInMillis())
				+ "' WHERE UUID = '" + r.getPlayer().getUniqueId().toString()
				+ "';";
		
		if(connect() instanceof Connection){
			try {
				s=con.createStatement();
				s.executeUpdate(date);
				s.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
			
		}
		return false;
	}

	/**
	 * Writes the given player info to the database
	 * 
	 * @param r Raider
	 * @return if write successful
	 */
	public static boolean writePlayerInfo(Raider r){
		Statement s1,s2;
		//set the local update query
		String local="UPDATE " + Constants.serverName
				+ "_USERS"
				+ " SET RATING = " + r.getLocalRating()
				+ " WHERE UUID = '" + r.getPlayer().getUniqueId().toString()
				+ "';";

		//set the global update query
		String global="UPDATE "
				+ "USERS"
				+ " SET RATING = " + r.getLocalRating()
				+ " WHERE UUID = '" + r.getPlayer().getUniqueId().toString()
				+ "';";
		String global2="UPDATE "
				+ "USERS"
				+ " SET SIN = " + r.getSin()
				+ " WHERE UUID = '" + r.getPlayer().getUniqueId().toString()
				+ "';";


		if(connect() instanceof Connection){//ensure there is a connection
			try {
				//execute the queries
				s1=con.createStatement();
				s1.executeUpdate(local);
				s1.close();
				s2=con.createStatement();
				s2.executeUpdate(global);
				s2.executeUpdate(global2);
				s2.close();
				return true;

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}



		return false;
	}

	/**
	 * Read the player info from the database, and generate the corresponding Raider object
	 * 
	 * @param p Player
	 * @return Raider
	 */
	public static Raider readPlayerInfo(Player p){
		Raider r=new Raider(p);
		String UUID=p.getUniqueId().toString();//player UUID is primary key in DB
		String queryL,queryG,queryG2,queryDate;
		Statement stmt = null;
		Statement stmt2=null;
		ResultSet rs,rs2;
		java.sql.Date lastSeen;
		int timeNow = Calendar.getInstance().DAY_OF_YEAR;
		int yearNow=Calendar.getInstance().YEAR;

		if(connect() instanceof Connection){//ensure there is a connection
			//global query
			queryG=
					"SELECT *"
							+ " FROM USERS"
							+ " WHERE UUID = '"
							+ UUID +"';";
			
			//local query
			queryL=
					"SELECT RATING"
							+ " FROM " + Constants.serverName 
							+ "_USERS"
							+ " WHERE UUID = '"
							+ UUID +"';";
			try {
				stmt=con.createStatement();
				rs=stmt.executeQuery(queryL);//execute the Local query

				if (rs.next()){//get the returned row in the resultset if it exists
					r.setLocalRating(rs.getInt(1),true);//set local rating 
				}else{
					r.setLocalRating(0,true);
					registerPlayer(UUID,false);
					if(Constants.welcome){
						Utility.welcome(p);
					}
					Dungeons.getPlugin(Dungeons.class).log("Player new to server");
				}
				stmt.close();
				stmt2=con.createStatement();
				rs2=stmt2.executeQuery(queryG);//excute global query
				if (rs2.next()){
					r.setGlobalRating(rs2.getInt(2),true);//set global rating
					r.setSin(rs2.getInt(3), true);
					lastSeen=rs2.getDate(4);
					Calendar cal=Calendar.getInstance();
					cal.setTime(lastSeen);
					//int diff=timeNow-cal.DAY_OF_YEAR;
					if(timeNow>cal.DAY_OF_YEAR ||yearNow>cal.YEAR ){
						int mod;
						if(yearNow>cal.YEAR){
							mod=365-cal.DAY_OF_YEAR+timeNow;
						}else{
							mod=timeNow-cal.DAY_OF_YEAR;
						}
						r.setLocalRating((int)(rs.getInt(2)-(mod)),false);//set rating
						writePlayerInfo(r);
					}
					
				}else{
					r.setGlobalRating(0,true);
					registerPlayer(UUID,true);
					Dungeons.getPlugin(Dungeons.class).log("Player new to network");
				}
				stmt2.close();
				
//				stmt2=con.createStatement();
//				rs2=stmt2.executeQuery(queryG2);//excute global query
//				if (rs2.next()){
//					r.setSin(rs2.getInt(1),true);//set global rating
//				}else{
//					r.setGlobalRating(0,true);
//					registerPlayer(UUID,true);
//					Dungeons.getPlugin(Dungeons.class).log("Player new to network");
//				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

	/**
	 * Creates new global and/or local entries for a new player
	 * 
	 * @param UUID
	 * @param global
	 */
	private static void registerPlayer(String UUID,boolean global){
		String query;
		Statement stmt;

		if (global){//check if local or global, set appropriate query
			query=
					"INSERT INTO"

					+ " USERS"
					+ " VALUES ('"
					+ UUID + "',0,0,'"+new java.sql.Date(Calendar.getInstance().getTimeInMillis())+"');";

		}else{
			query=
					"INSERT INTO "
					//+ Constants.dbName + "."
					+ Constants.serverName
					+ "_USERS "
					+ "VALUES ('"
					+ UUID + "',0);";
		}
		if (connect() instanceof Connection){//ensure connection

			try {
				Dungeons.getPlugin(Dungeons.class).log(query);
				Dungeons.getPlugin(Dungeons.class).log("Registering player");
				stmt=con.createStatement();
				stmt.executeUpdate(query);//execute query
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	/**
	 *Ensures the required tables exist
	 *
	 */
	public static void createTables(){
		
		//local table query
		String userLocalTable=
				"CREATE TABLE IF NOT EXISTS " //+ Constants.dbName + "."
						+ Constants.serverName +
						"_USERS " +
						"(UUID varchar(36) NOT NULL, " +
						"RATING int NOT NULL, " +
						"PRIMARY KEY (UUID))";
		
		//global table query
		String userGlobalTable=
				"CREATE TABLE IF NOT EXISTS " + //Constants.dbName + "."
						"USERS " +
						"(UUID varchar(36) NOT NULL, " +
						"RATING int NOT NULL, " +
						"SIN int NOT NULL, " +
						"LASTSEEN DATE NOT NULL, " +
						"PRIMARY KEY (UUID))";


		Statement stmt=null;

		if (connect() instanceof Connection){//ensure connection

			try {//execute queries
				stmt = con.createStatement();
				stmt.executeUpdate(userLocalTable);
				stmt.close();
				stmt = con.createStatement();
				stmt.executeUpdate(userGlobalTable);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) { try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
			}
		}


	}
	
	/**
	 * wipes all data from local and global tables
	 */
	public static void wipeTables(){
		//set queries
		String userLocalTable=
				"TRUNCATE TABLE "
						+ Constants.serverName +
						"_USERS;";
		String userGlobalTable=
				"TRUNCATE TABLE " +
						"USERS;";


		Statement stmt=null;

		if (connect() instanceof Connection){//ensure connection

			try {//execute queries
				stmt = con.createStatement();
				stmt.executeUpdate(userLocalTable);
				stmt.close();
				stmt = con.createStatement();
				stmt.executeUpdate(userGlobalTable);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) { try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
			}
		}
		
	}

}
